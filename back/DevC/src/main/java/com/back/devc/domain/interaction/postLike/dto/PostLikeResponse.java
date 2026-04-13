package com.back.devc.domain.interaction.postLike.dto;

public record PostLikeResponse(
        int postId,
        boolean liked,
        int likeCount
) {
}