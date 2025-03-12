package com.example.cherrypickdemo.domain.board.dto.response;

import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardResponse {

    private int boardId;
    private String title;
    private String content;
    private int price;
    private String username;
    private Set<String> tags;
}
