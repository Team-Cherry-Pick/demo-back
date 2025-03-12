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

            String content = "내용";
            int price = 10000;

            Optional<HashTag> existingTagOpt = hashTagRepository.findByTagName("해시태그");
            HashTag existingTag;
            if (existingTagOpt.isPresent()) {
                existingTag = existingTagOpt.get();
            } else {
                HashTag dummyTag = new HashTag();
                dummyTag.setTagName("해시태그");
                hashTagRepository.save(dummyTag);
                existingTag = dummyTag;
            }

            Set<HashTag> hashTags = new HashSet<>();
            hashTags.add(existingTag);

            User dummyUser = userRepository.findById(1L).orElse(null);

            Board board = new Board();
            board.setTitle(title);
            board.setContent(content);
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
}
