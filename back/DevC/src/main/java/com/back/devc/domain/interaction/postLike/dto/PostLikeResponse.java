package com.back.devc.domain.interaction.postLike.dto;

public record PostLikeResponse(
        long postId,
        boolean liked,
        long likeCount
) {
}