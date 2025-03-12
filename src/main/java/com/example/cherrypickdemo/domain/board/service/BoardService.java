package com.example.cherrypickdemo.domain.board.service;

import com.example.cherrypickdemo.domain.board.dto.request.BoardRequest;
import com.example.cherrypickdemo.domain.board.dto.response.BoardResponse;
import com.example.cherrypickdemo.domain.board.entity.Board;
import com.example.cherrypickdemo.domain.hashtag.entity.HashTag;
import com.example.cherrypickdemo.domain.hashtag.repository.HashTagRepository;
import com.example.cherrypickdemo.domain.user.entity.User;
import com.example.cherrypickdemo.domain.user.repository.UserRepository;
import com.example.cherrypickdemo.domain.board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final HashTagRepository hashTagRepository;

    // 게시판 생성
    public ResponseEntity<?> createBoard(BoardRequest boardRequest) {
        Optional<User> userOptional = userRepository.findById(boardRequest.getUserId());

        if (userOptional.isEmpty()) {
            throw new RuntimeException("존재하지 않는 유저 정보입니다.");
        }

        User user = userOptional.get();

        Set<HashTag> hashTags = new HashSet<>();

        // 전달된 해시태그 이름을 처리
        for (String tagName : boardRequest.getTagNames()) {
            Optional<HashTag> existingHashTag = hashTagRepository.findByTagName(tagName);

            // 해시태그가 이미 존재하면 추가, 존재하지 않으면 새로운 해시태그를 저장
            HashTag hashTag = existingHashTag.orElseGet(() -> {
                HashTag newTag = new HashTag();
                newTag.setTagName(tagName);
                return hashTagRepository.save(newTag);
            });

            hashTags.add(hashTag);
        }

        Board board = new Board();
        board.setTitle(boardRequest.getTitle());
        board.setContent(boardRequest.getContent());
        board.setPrice(boardRequest.getPrice());
        board.setUser(user);
        board.setHashTags(hashTags);

        boardRepository.save(board);

        return ResponseEntity.status(HttpStatus.CREATED).body("{\"message\":\"게시글 생성 성공\"}");
    }

    // 게시글 상세 조회
    public BoardResponse getBoardDetails(int boardId) {
        Optional<Board> boardOptional = boardRepository.findById(boardId);

        if (boardOptional.isEmpty()) {
            throw new RuntimeException("존재하지 않는 게시글입니다.");
        }

        Board board = boardOptional.get();

        Set<HashTag> hashTags = board.getHashTags();
        User user = board.getUser();

        BoardResponse response = new BoardResponse();
        response.setBoardId(board.getBoardId());
        response.setTitle(board.getTitle());
        response.setContent(board.getContent());
        response.setPrice(board.getPrice());
        response.setUsername(user.getUsername());
        response.setTags(hashTags.stream()
                .map(HashTag::getTagName)
                .collect(Collectors.toSet()));

        return response;
    }
}
