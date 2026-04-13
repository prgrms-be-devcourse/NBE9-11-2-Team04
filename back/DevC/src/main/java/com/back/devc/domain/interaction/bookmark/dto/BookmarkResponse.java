package com.back.devc.domain.interaction.bookmark.dto;

public record BookmarkResponse(
        int postId,
        boolean bookmarked
) {
}