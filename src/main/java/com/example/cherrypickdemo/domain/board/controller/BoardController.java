package com.example.cherrypickdemo.domain.board.controller;

import com.example.cherrypickdemo.domain.board.dto.request.BoardRequest;
import com.example.cherrypickdemo.domain.board.dto.response.BoardResponse;
import com.example.cherrypickdemo.domain.board.service.BoardCrawlService;
import com.example.cherrypickdemo.domain.board.service.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/board")
public class BoardController {

    @Autowired
    private BoardService boardService;

    @Autowired
    private BoardCrawlService boardCrawlService;

    // 게시글 생성 API
    @PostMapping("/create")
    public ResponseEntity<?> createBoard(@RequestBody BoardRequest boardRequest) {
        return boardService.createBoard(boardRequest);
    }

    // 게시글 상세 조회 API
    @GetMapping("/{boardId}")
    public ResponseEntity<BoardResponse> getBoardDetails(@PathVariable int boardId) {
        try {
            BoardResponse response = boardService.getBoardDetails(boardId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new BoardResponse());  // 게시글 없을 경우 404 반환
        }
    }

    // 크롤링 API
    @GetMapping("/crawl-board")
    public String crawlBoard() {

        String[] urls = {
                "https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu&page=1",
                "https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu&page=2&divpage=99",
                "https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu&page=3&divpage=99",
                "https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu&page=4&divpage=99",
        };

        try {
            for (String url : urls) {
                boardCrawlService.crawlAndSaveBoard(url);
            }
            return "게시글 크롤링 및 저장 완료";
        } catch (Exception e) {
            return "오류 발생: " + e.getMessage();
        }
    }
}
