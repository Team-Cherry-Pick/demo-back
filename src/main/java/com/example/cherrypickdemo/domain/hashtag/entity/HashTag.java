package com.example.cherrypickdemo.domain.hashtag.entity;

import com.example.cherrypickdemo.domain.board.entity.Board;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
public class HashTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int tagId;

    private String tagName;

    @ManyToMany(mappedBy = "hashTags")
    private Set<Board> boards;
}
