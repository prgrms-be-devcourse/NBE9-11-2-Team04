package com.back.devc.domain.post.post.dto;

import com.back.devc.domain.post.post.entity.Post;

import lombok.Builder;

@Builder
public record PostCreateResponse(
        Long postId
) {
    public static PostCreateResponse from(Post post) {
        return PostCreateResponse.builder()
                .postId(post.getPostId())
                .build();
    }
}