package com.example.cherrypickdemo.domain.board.dto.response;

import java.util.Set;
import java.util.stream.Collectors;

import com.example.cherrypickdemo.domain.board.entity.Board;
import lombok.*;

@Getter
@Setter @RequiredArgsConstructor @AllArgsConstructor
@Builder
public class BoardResponse {

    private int boardId;
    private String title;
    private String content;
    private int price;
    private String username;
    private Set<String> tags;

    public static BoardResponse of(Board board) {
        return BoardResponse.builder()
                .boardId(board.getBoardId())
                .title(board.getTitle())
                .content(board.getContent())
                .price(board.getPrice())
                .username(board.getUser().getUsername())
                .tags(board.getHashTags().stream().map(tag -> tag.getTagName()).collect(Collectors.toSet()))
                .build();
    }

}
