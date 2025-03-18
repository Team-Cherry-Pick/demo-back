package com.example.cherrypickdemo.domain.board.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BoardListResponse {
    private int totalCount;  // 이번에 조회된 데이터 개수
    private List<BoardResponse> content;  // 게시글 목록
    private boolean isFirst;  // 첫 페이지 여부
    private boolean isLast;  // 마지막 페이지 여부
    private boolean hasNext;  // 다음 페이지 여부
    private boolean hasPrevious;  // 이전 페이지 여부
}
