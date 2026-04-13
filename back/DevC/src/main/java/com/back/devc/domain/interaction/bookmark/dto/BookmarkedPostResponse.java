package com.back.devc.domain.interaction.bookmark.dto;

import java.time.LocalDateTime;

public record BookmarkedPostResponse(
        int postId,
        String title,
        String authorNickname,
        int likeCount,
        int commentCount,
        LocalDateTime createdAt
) {
}