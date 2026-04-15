package com.back.devc.domain.member.mypage.dto;

import java.time.LocalDateTime;

public record MyCommentResponse(
        Long commentId,
        Long postId,
        String content,
        LocalDateTime createdAt
) {
}