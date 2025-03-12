package com.example.cherrypickdemo.domain.user.entity;

import com.example.cherrypickdemo.domain.board.entity.Board;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;

    private String username;
    private String password;

    @OneToMany(mappedBy = "user")
    private Set<Board> boards;
}
