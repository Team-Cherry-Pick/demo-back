package com.example.cherrypickdemo.domain.board.service;

import com.example.cherrypickdemo.domain.board.entity.Board;
import com.example.cherrypickdemo.domain.board.repository.BoardRepository;
import com.example.cherrypickdemo.domain.hashtag.entity.HashTag;
import com.example.cherrypickdemo.domain.hashtag.repository.HashTagRepository;
import com.example.cherrypickdemo.domain.user.entity.User;
import com.example.cherrypickdemo.domain.user.repository.UserRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BoardCrawlService {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HashTagRepository hashTagRepository;

    // 크롤링
    public void crawlAndSaveBoard(String url) throws IOException {
        Document document = Jsoup.connect(url).get();

        Elements rows = document.select("tr.baseList.bbs_new1");

        List<String> forbiddenWords = List.of("다양", "가격다양", "가격 다양");

        int count = 0;
        for (Element row : rows) {
            if (count >= 100) break;
            count++;

            String title = row.select(".baseList-title span").text();

            if (containsForbiddenWord(title, forbiddenWords)) {
                continue;
            }

            // 챗GPT에게 타이틀을 전달해서 가격과 해시태그 추출하는 로직 (가정)
            // 예시: 응답받은 데이터 -> "10000/건강식품,저칼로리,저당,탄산수,다이어트"
            String response = "999/건강식품,저칼로리,저당,탄산수,다이어트";  // 예시

            // 가격과 해시태그 추출
            int price = extractPrice(response);  // 가격 추출
            Set<HashTag> hashTags = extractHashTags(response);  // 해시태그 추출

            User dummyUser = userRepository.findById(1L).orElse(null);

            // Board 엔티티 생성 및 저장
            Board board = new Board();
            board.setTitle(title);
            board.setContent("내용");
            board.setPrice(price);  // 가격 저장
            board.setHashTags(hashTags);  // 해시태그 저장
            board.setUser(dummyUser);

            boardRepository.save(board);
        }
    }

    private boolean containsForbiddenWord(String title, List<String> forbiddenWords) {
        for (String forbiddenWord : forbiddenWords) {
            if (title.contains(forbiddenWord)) {
                return true;
            }
        }
        return false;
    }

    // 정규 표현식으로 가격 추출
    private int extractPrice(String response) {
        Pattern pattern = Pattern.compile("^(\\d+)");  // 가격은 첫 번째 숫자
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));  // 가격을 반환
        }
        return 0;
    }

    // 정규 표현식으로 해시태그 추출
    private Set<HashTag> extractHashTags(String response) {
        Set<HashTag> hashTags = new HashSet<>();

        // 가격 뒤의 '/' 이후에 해시태그가 오는 부분을 추출
        String tagsPart = response.split("/")[1];  // "건강식품,저칼로리,저당,탄산수,다이어트"

        Pattern pattern = Pattern.compile("([\\w가-힣]+)");  // 해시태그는 알파벳, 숫자, 한글로 구성
        Matcher matcher = pattern.matcher(tagsPart);

        while (matcher.find()) {
            String tagName = matcher.group(1);
            Optional<HashTag> existingTagOpt = hashTagRepository.findByTagName(tagName);
            HashTag existingTag;
            if (existingTagOpt.isPresent()) {
                existingTag = existingTagOpt.get();
            } else {
                HashTag newTag = new HashTag();
                newTag.setTagName(tagName);
                hashTagRepository.save(newTag);
                existingTag = newTag;
            }
            hashTags.add(existingTag);  // 해시태그 추가
        }
        return hashTags;
    }
}
