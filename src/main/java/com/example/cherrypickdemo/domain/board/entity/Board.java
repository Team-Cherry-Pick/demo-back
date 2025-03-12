package com.example.cherrypickdemo.domain.board.entity;

import com.example.cherrypickdemo.domain.hashtag.entity.HashTag;
import com.example.cherrypickdemo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Getter
@Setter
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int boardId;

    private String title;
    private String content;
    private int price;

    @ManyToMany
    @JoinTable(
            name = "board_hashtag",
            joinColumns = @JoinColumn(name = "board_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<HashTag> hashTags;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
