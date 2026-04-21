package com.back.devc.domain.interaction.postLike.dto;

import lombok.Builder;

/**
 * 좋아요 추가/취소 응답 DTO
 */
@Builder
public record PostLikeResponse(
        Long postId,
        boolean liked,
        long likeCount,
        String message
) {
}