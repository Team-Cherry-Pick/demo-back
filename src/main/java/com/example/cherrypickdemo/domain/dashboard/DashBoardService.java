package com.example.cherrypickdemo.domain.dashboard;

import com.example.cherrypickdemo.domain.board.dto.response.BoardResponse;
import com.example.cherrypickdemo.domain.board.entity.Board;
import com.example.cherrypickdemo.domain.board.repository.BoardRepository;
import com.example.cherrypickdemo.domain.dashboard.dto.LogRequestDto;
import com.example.cherrypickdemo.domain.dashboard.dto.LogResponseDto;
import com.example.cherrypickdemo.domain.hashtag.repository.HashTagRepository;
import io.lettuce.core.AbstractRedisAsyncCommands;
import org.springframework.data.domain.Range;
import io.lettuce.core.StreamMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor @Slf4j
public class DashBoardService
{
    private final RedisTemplate<String, Object> redisTemplate;
    private final BoardRepository boardRepository;
    private final HashTagRepository hashTagRepository;
    final String STREAM_NAME = "USER_BEHAVIOR_STREAM";

    public String addLog(LogRequestDto logRequestDto)
    {
        log.info(logRequestDto.toString());
        var rId = redisTemplate.opsForStream().add(STREAM_NAME, logRequestDto.toMap());
        log.info(rId.getValue());

        return rId.getValue();
    }

    public HashMap<String, Object> getLogByUserId(int userId) {
        log.info("로그조회 시작");
        StreamOperations<String, String, String> streamOps = redisTemplate.opsForStream();
        var response = new HashMap<String, Object>();
        // 모든 메시지 조회: ID 범위 "-" to "+"
        List<MapRecord<String, String, String>> records = streamOps.range(STREAM_NAME, Range.unbounded());

        List<LogResponseDto> logList = new ArrayList<>();
        for (MapRecord<String, String, String> record : records) {
            var id = record.getId().getValue();
            String timestamp = id.split("-")[0];
            LocalDateTime createdAt = Instant.ofEpochMilli(Long.parseLong(timestamp))
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .toLocalDateTime();
            var map = record.getValue();
            var title = boardRepository.findById(Integer.parseInt(map.get("boardId"))).map(Board::getTitle).get();
            logList.add(LogResponseDto.builder()
                    .logId(id)
                    .createdAt(createdAt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")))
                    .targetItemId(map.get("boardId"))
                    .targetItemName(title)
                    .method(LogMethodEnum.valueOf(map.get("method")))
                    .build());
        }

        Collections.reverse(logList);
        response.put("logs", logList);
        log.info("로그조회 끝");
        return response;
    }

    public Map<String, Object> getUserInterestWeight(int userId)
    {
        var zSetOps = redisTemplate.opsForZSet();
        var set = zSetOps.reverseRangeWithScores("user:" + String.valueOf(userId) + ":interests", 0, 29);

        var response = new HashMap<String, Object>();
        var tags = new ArrayList<Map<String, String>>();
        for(var s : set){
            var tag = new HashMap<String, String>();
            String member = s.getValue().toString();
            Double score = s.getScore();

            var name = hashTagRepository.findById(Integer.valueOf(member)).map(h -> h.getTagName()).get();
            tag.put("tagName", name);
            tag.put("weight", String.valueOf(new BigDecimal(score).setScale(5, RoundingMode.HALF_UP)));
            tags.add(tag);
        }

        response.put("tags", tags);
        response.put("userId", userId);

        return response;
    }

    public HashMap<String, Object> getTagsSimilarity(String name)
    {
        var response = new HashMap<String, Object>();
        var tags = new ArrayList<Map<String, Object>>();
        var list = hashTagRepository.findAllByTagName(name);
        log.info(list.toString());

        for(var tagId : list){
            var zSetOps = redisTemplate.opsForZSet();
            String key = "tag:similarity:" + tagId.toString();

            if(!redisTemplate.hasKey(key)) continue;

            var set = zSetOps.reverseRangeWithScores(key, 1, 15);
            var priceMap = redisTemplate.opsForHash().entries(key+":price");
            var titleMap = redisTemplate.opsForHash().entries(key+":title");

            var mainTag = new HashMap<String, Object>();
            var subTags = new ArrayList<HashMap<String, String>>();
            int rank = 0;
            for(var s : set){
                var tag = new HashMap<String, String>();
                String subTagId = s.getValue().toString();
                Double score = s.getScore();

                var price = priceMap.get(subTagId);
                var title = titleMap.get(subTagId);

                log.info(price.toString());

                var subTagName = hashTagRepository.findById(Integer.valueOf(subTagId)).map(h -> h.getTagName()).get();

                tag.put("rank", String.valueOf(rank));
                tag.put("tagName", subTagName);
                tag.put("similarityScore", String.valueOf(new BigDecimal(score).setScale(5, RoundingMode.HALF_UP)));
                tag.put("priceSimilarity", String.valueOf(new BigDecimal(String.valueOf(price)).setScale(5, RoundingMode.HALF_UP)));
                tag.put("titleSimilarity", String.valueOf(new BigDecimal(String.valueOf(title)).setScale(5, RoundingMode.HALF_UP)));
                tag.put("valuable", String.valueOf(score>0.5));

                rank++;

                subTags.add(tag);
            }

            var mainTagName = hashTagRepository.findById(Integer.valueOf(tagId)).map(h -> h.getTagName()).get();
            mainTag.put("tagName", mainTagName);
            mainTag.put("similarTags", subTags);
            tags.add(mainTag);
        }
        response.put("length", tags.size());
        response.put("tags", tags);
        return response;

    }

    public HashMap<String, Object> getInterestBoard(Integer userId)
    {
        var key = "user:" + String.valueOf(userId) + ":interests";

        var response = new HashMap<String, Object>();

        // 관심 해쉬태그 불러오기
        var userHashTag = redisTemplate.opsForZSet().reverseRange(key,1, 10);
        var userHashTagIds = userHashTag.stream().map(u -> Integer.parseInt(u.toString())).toList();//userHashTag.stream().map(t -> Integer.parseInt(t))

        // 각 관심해쉬태그에서 상품 두개씩 가져오기
        var boards = boardRepository.findBoardsByTagId(userHashTagIds, 20).stream().map(b -> BoardResponse.of(b)).collect(Collectors.toList());

        response.put("length", boards.size());
        response.put("userId", userId);
        response.put("boards", boards);
        log.info(boards.toString());

        return response;
    }

}
