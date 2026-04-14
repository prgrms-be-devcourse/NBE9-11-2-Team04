package com.back.devc.domain.post.post.dto;

import com.back.devc.domain.post.post.entity.Post;

public record PostDeleteResponse(
        Long postId,
        String message
) {
}