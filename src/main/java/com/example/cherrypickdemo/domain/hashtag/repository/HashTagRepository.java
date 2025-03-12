package com.example.cherrypickdemo.domain.hashtag.repository;

import com.example.cherrypickdemo.domain.hashtag.entity.HashTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HashTagRepository extends JpaRepository<HashTag, Integer> {
    Optional<HashTag> findByTagName(String tagName);

}
