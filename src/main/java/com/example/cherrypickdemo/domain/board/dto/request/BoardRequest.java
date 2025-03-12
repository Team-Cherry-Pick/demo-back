package com.example.cherrypickdemo.domain.board.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BoardRequest {
    private String title;
    private String content;
    private int price;
    private long userId;
    private List<String> tagNames = new ArrayList<>();
}
