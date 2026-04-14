package com.back.devc.domain.interaction.postLike.dto;

import java.time.LocalDateTime;

public record LikedPostResponse(
        long postId,
        String title,
        String authorNickname,
        long likeCount,
        long commentCount,
        LocalDateTime createdAt
) {
}