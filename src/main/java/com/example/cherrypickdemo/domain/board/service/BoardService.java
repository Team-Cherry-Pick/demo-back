package com.example.cherrypickdemo.domain.board.service;

import com.example.cherrypickdemo.domain.board.dto.request.BoardRequest;
import com.example.cherrypickdemo.domain.board.dto.response.BoardListResponse;
import com.example.cherrypickdemo.domain.board.dto.response.BoardResponse;
import com.example.cherrypickdemo.domain.board.entity.Board;
import com.example.cherrypickdemo.domain.hashtag.entity.HashTag;
import com.example.cherrypickdemo.domain.hashtag.repository.HashTagRepository;
import com.example.cherrypickdemo.domain.user.entity.User;
import com.example.cherrypickdemo.domain.user.repository.UserRepository;
import com.example.cherrypickdemo.domain.board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
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

    // 게시글 전체 조회 (10개씩 고정)
    public BoardListResponse getAllBoards(int page) {
        int pageSize = 10; // 한 페이지에 10개씩 고정
        Pageable pageable = PageRequest.of(page - 1, pageSize);  // 페이지 번호를 1부터 받으므로 -1 처리
        Page<Board> boardPage = boardRepository.findAll(pageable);

        // BoardResponse로 변환
        List<BoardResponse> boardResponses = boardPage.getContent().stream()
                .map(board -> {
                    BoardResponse response = new BoardResponse();
                    response.setBoardId(board.getBoardId());
                    response.setTitle(board.getTitle());
                    response.setContent(board.getContent());
                    response.setPrice(board.getPrice());
                    response.setUsername(board.getUser().getUsername());
                    response.setTags(board.getHashTags().stream()
                            .map(HashTag::getTagName)
                            .collect(Collectors.toSet()));
                    return response;
                })
                .collect(Collectors.toList());

        // BoardListResponse 객체 생성 및 반환
        BoardListResponse response = new BoardListResponse();
        response.setTotalCount(boardPage.getNumberOfElements());  // 이번 페이지에서 조회된 데이터 개수
        response.setContent(boardResponses);  // 게시글 목록
        response.setFirst(boardPage.isFirst());  // 첫 페이지 여부
        response.setLast(boardPage.isLast());  // 마지막 페이지 여부
        response.setHasNext(boardPage.hasNext());  // 다음 페이지 여부
        response.setHasPrevious(boardPage.hasPrevious());  // 이전 페이지 여부

        return response;
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
