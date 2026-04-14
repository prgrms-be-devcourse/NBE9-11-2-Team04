package com.back.devc.domain.post.post.dto;

import com.back.devc.domain.post.post.entity.Post;

public record PostUpdateResponse(
        Long postId,
        String title,
        String content,
        Long categoryId
) {
    public static PostUpdateResponse from(Post post) {
        return new PostUpdateResponse(
                post.getPostId(),
                post.getTitle(),
                post.getContent(),
                post.getCategory().getCategoryId()
        );
    }
}