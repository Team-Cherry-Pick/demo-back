package com.example.cherrypickdemo.domain.dashboard.dto;

import com.example.cherrypickdemo.domain.dashboard.LogMethodEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public record LogRequestDto(
    int userId,
    int boardId,
    String method
)
{
    public String toJson() {
        Map<String, String> map = new HashMap<>();
        map.put("userId", String.valueOf(userId));
        map.put("boardId", String.valueOf(boardId));
        map.put("method", String.valueOf(method));
        ObjectMapper objectMapper = new ObjectMapper();
        String json = null;
        try {
            json = objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return json;
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        map.put("userId", String.valueOf(userId));
        map.put("boardId", String.valueOf(boardId));
        map.put("method", String.valueOf(method));

        return map;
    }
}
