package com.back.devc.domain.post.post.dto;

import com.back.devc.domain.post.post.entity.Post;

public record PostCreateResponse(
        Long postId,
        String message
) {
    public static PostCreateResponse from(Post post) {
        return new PostCreateResponse(
                post.getPostId(),
                "게시글이 생성되었습니다."
        );
    }
}