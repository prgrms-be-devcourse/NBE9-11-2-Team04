package com.back.devc.domain.interaction.postLike.dto;

public record PostLikeResponse(
        Long postId,
        boolean liked,
        int likeCount,
        String message
) {
}