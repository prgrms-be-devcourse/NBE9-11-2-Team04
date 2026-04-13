package com.back.devc.domain.interaction.postLike.dto;

import java.time.LocalDateTime;

public record LikedPostResponse(
        int postId,
        String title,
        String authorNickname,
        int likeCount,
        int commentCount,
        LocalDateTime createdAt
) {
}