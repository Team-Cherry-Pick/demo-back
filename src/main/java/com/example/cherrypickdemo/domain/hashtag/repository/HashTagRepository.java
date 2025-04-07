package com.example.cherrypickdemo.domain.hashtag.repository;

import com.example.cherrypickdemo.domain.board.entity.Board;
import com.example.cherrypickdemo.domain.hashtag.entity.HashTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HashTagRepository extends JpaRepository<HashTag, Integer> {
    Optional<HashTag> findByTagName(String tagName);
    @Query(value = "Select tag_id from hash_tag where tag_name like %:name% LIMIT 20;", nativeQuery = true)
    List<Integer> findAllByTagName(@Param("name") String name);


}
