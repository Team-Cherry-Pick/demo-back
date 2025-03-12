package com.example.cherrypickdemo.domain.board.repository;

import com.example.cherrypickdemo.domain.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Integer> {
}
