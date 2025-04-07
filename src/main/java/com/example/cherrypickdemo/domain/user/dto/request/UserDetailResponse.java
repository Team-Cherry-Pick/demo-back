package com.example.cherrypickdemo.domain.user.dto.request;

import com.example.cherrypickdemo.domain.user.entity.User;
import lombok.Builder;

@Builder
public record UserDetailResponse(
        int userId,
        String userName
)
{
    public static UserDetailResponse of(User user) {

        return UserDetailResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUsername())
                .build();
    }

}
