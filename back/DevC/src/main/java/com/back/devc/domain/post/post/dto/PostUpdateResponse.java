package com.back.devc.domain.post.post.dto;

import com.back.devc.domain.post.post.entity.Post;

import lombok.Builder;

@Builder
public record PostUpdateResponse(
        Long postId,
        String title,
        String content,
        Long categoryId
) {
    public static PostUpdateResponse from(Post post) {

        var category = post.getCategory();

        return PostUpdateResponse.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .content(post.getContent())
                .categoryId(category != null ? category.getCategoryId() : null)
                .build();
    }
}