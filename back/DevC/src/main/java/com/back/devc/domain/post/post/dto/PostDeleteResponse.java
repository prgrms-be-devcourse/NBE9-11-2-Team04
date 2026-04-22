package com.back.devc.domain.post.post.dto;

import lombok.Builder;

@Builder
public record PostDeleteResponse(
        Long postId
) {
    public static PostDeleteResponse of(Long postId) {
        return PostDeleteResponse.builder()
                .postId(postId)
                .build();
    }
}