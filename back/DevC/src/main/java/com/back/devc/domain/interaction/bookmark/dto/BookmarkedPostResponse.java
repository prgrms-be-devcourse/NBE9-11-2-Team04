package com.back.devc.domain.interaction.bookmark.dto;

import java.time.LocalDateTime;

public record BookmarkedPostResponse(
        long postId,
        String title,
        String authorNickname,
        long likeCount,
        long commentCount,
        LocalDateTime createdAt
) {
}