package com.example.cherrypickdemo.domain.dashboard.dto;

import com.example.cherrypickdemo.domain.dashboard.LogMethodEnum;
import lombok.Builder;

@Builder
public record LogResponseDto(
        String logId,
        String targetItemId,
        String targetItemName,
        LogMethodEnum method,
        String createdAt
)
{

}
