package com.example.cherrypickdemo.domain.board.repository;

import com.example.cherrypickdemo.domain.board.entity.Board;
import com.example.cherrypickdemo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Integer> {
    @Query(value = "SELECT b.* " +
            "FROM board b " +
            "         INNER JOIN board_hashtag hb ON b.board_id = hb.board_id " +
            "         INNER JOIN hash_tag h ON hb.tag_id = h.tag_id " +
            "WHERE h.tag_id IN :tagIds " +
            "ORDER BY RAND() LIMIT :count;", nativeQuery = true)
    List<Board> findBoardsByTagId(@Param("tagIds")List<Integer> tagIds, @Param("count") int count);

    List<Integer> user(User user);
}
