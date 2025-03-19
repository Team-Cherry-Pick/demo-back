package com.example.cherrypickdemo.domain.board.service;

import com.example.cherrypickdemo.domain.board.entity.Board;
import com.example.cherrypickdemo.domain.board.repository.BoardRepository;
import com.example.cherrypickdemo.domain.hashtag.entity.HashTag;
import com.example.cherrypickdemo.domain.hashtag.repository.HashTagRepository;
import com.example.cherrypickdemo.domain.user.entity.User;
import com.example.cherrypickdemo.domain.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class BoardCrawlService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final HashTagRepository hashTagRepository;

    @Value("${openai.api-key}")
    private String openAiApiKey;

    @Value("${openai.api-url}")
    private String openAiApiUrl;

    // 크롤링
    public void crawlAndSaveBoard(String url) throws IOException {
        Document document = Jsoup.connect(url).get();

        Elements rows = document.select("tr.baseList.bbs_new1");

        List<String> forbiddenWords = List.of("다양", "가격다양", "가격 다양", "본문참조", "참조");

        int count = 0;
        for (Element row : rows) {

            String title = row.select(".baseList-title span").text();

            if (containsForbiddenWord(title, forbiddenWords)) {
                continue;
            }

            String response = getChatGPTResponse(title);

            // GPT 응답이 유추 불가인 경우 저장 안 함
            if (response.contains("유추 불가")) {
                System.out.println("GPT 응답에 '유추 불가'가 포함되어 저장하지 않습니다: " + title);
                continue;
            }

            int price = extractPrice(response);

            // 가격이 1000원 이하인 경우 저장하지 않음
            if (price <= 1000) {
                System.out.println("가격이 1000원 이하이므로 저장하지 않습니다: " + title);
                continue;
            }

            Set<HashTag> hashTags = extractHashTags(response);

            User dummyUser = userRepository.findById(1L).orElse(null);

            Board board = new Board();
            board.setTitle(title);
            board.setContent("내용");
            board.setPrice(price);
            board.setHashTags(hashTags);
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

    // OpenAI API
    private String getChatGPTResponse(String title) {
        RestTemplate restTemplate = new RestTemplate();

        String prompt = title + "\n 이 제목을 보고 가격과 해시태그 20개를 뽑아줘. " +
                "해시태그는 전자기기, 가전제품, 패션, 뷰티/헬스, 홈/리빙, 식품, 스포츠/레저, 자동차, 책/문구, 취미/완구, 게임, 영화, 음악, 여행, 애완동물, 유아용품, 정원용품, 세차용품, 컴퓨터 부품, 사진/영상, 오피스 용품, 의료기기, DIY 용품, 시계, 쥬얼리, 음향기기, 스마트홈, 교육용품, 선물, 공구, 텔레비전, 스마트폰 액세서리, 인테리어 소품, 화장품 중에서 해당하는걸 최대한 고르고" +
                "나머지는 아무 단어나 가져오지말고 제목을 보고 유추해서 최대한 제품성과 관련된 단어로 너가 해시태그를 선정해줘. 특가, 무료배송 이런거 말고 내가 위에 보낸 해시태그 예시들처럼 최대한 포괄적인 단어로 선정하면 돼." +
                "해시태그엔 # 표시를 붙이지말고 단어만 쉼표로 구분해서 보내줘." +
                "예시를 보여줄게, 아무 것도 바꾸지말고 무조건 다음 형식을 지켜서 답해줘. 가격:10000, 해시태그: 과일, 식품, 사과, 유기농, 건강식품, 선물" +
                "만약 가격이나 해시태그를 정확히 알아내기 힘든 제목이라면 무조건 정확히 '유추 불가'라고 대답해줘.";

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("model", "gpt-3.5-turbo");
        requestMap.put("max_tokens", 300);
        requestMap.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = "";
        try {
            requestBody = objectMapper.writeValueAsString(requestMap);
        } catch (JsonProcessingException e) {
            System.err.println("요청 본문 직렬화 중 오류 발생: " + e.getMessage());
            return "";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + openAiApiKey);

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(openAiApiUrl, HttpMethod.POST, request, String.class);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                String response = responseEntity.getBody();

                JsonNode rootNode = objectMapper.readTree(response);
                String content = rootNode.path("choices").get(0).path("message").path("content").asText();

                return content;
            } else {
                System.err.println("OpenAI API 요청 실패: " + responseEntity.getStatusCode());
                return "";
            }
        } catch (IOException e) {
            System.err.println("OpenAI API 호출 중 오류 발생: " + e.getMessage());
            return "";
        }
    }

    private int extractPrice(String response) {
        // "가격: " 다음에 오는 숫자(쉼표 포함)를 찾음
        Pattern pattern = Pattern.compile("가격:\\s*([\\d,]+)");
        Matcher matcher = pattern.matcher(response);

        if (matcher.find()) {
            // 쉼표 제거 후 정수 변환
            String priceString = matcher.group(1).replaceAll(",", "");
            return Integer.parseInt(priceString);
        }
        return 0;
    }

    private Set<HashTag> extractHashTags(String response) {
        Set<HashTag> hashTags = new HashSet<>();

        // "해시태그: " 다음에 나오는 태그 리스트 찾기
        Pattern pattern = Pattern.compile("해시태그:\\s*(.+)");
        Matcher matcher = pattern.matcher(response);

        if (matcher.find()) {
            // 쉼표 기준으로 해시태그 나누기
            String[] tags = matcher.group(1).split("\\s*,\\s*");
            for (String tagName : tags) {
                // 공백 제거
                tagName = tagName.replaceAll("\\s+", "");

                // 점이 끝에 있을 경우 제거
                tagName = tagName.replaceAll("\\.$", "");

                // 해시태그 기호 제거
                tagName = tagName.replaceAll("^#+", "");

                // 이미 존재하는 해시태그인지 확인
                Optional<HashTag> existingTagOpt = hashTagRepository.findByTagName(tagName);
                HashTag existingTag;
                if (existingTagOpt.isPresent()) {
                    existingTag = existingTagOpt.get();
                } else {
                    // 새로운 해시태그 생성 및 저장
                    HashTag newTag = new HashTag();
                    newTag.setTagName(tagName);
                    hashTagRepository.save(newTag);
                    existingTag = newTag;
                }

                hashTags.add(existingTag);
            }
        }
        return hashTags;
    }
}

