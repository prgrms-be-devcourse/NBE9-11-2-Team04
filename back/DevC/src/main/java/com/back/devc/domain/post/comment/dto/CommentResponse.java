package com.back.devc.domain.post.comment.dto;

import com.back.devc.domain.post.comment.attachment.dto.CommentAttachmentResponse;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
public record CommentResponse(
        Long commentId,
        Long postId,
        String postTitle,
        Long userId,
        String nickname,
        Long parentCommentId,
        String content,
        boolean isDeleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<CommentResponse> replies,
        List<CommentAttachmentResponse> attachments
) {

    public static CommentResponse of(
            Long commentId,
            Long postId,
            String postTitle,
            Long userId,
            String nickname,
            Long parentCommentId,
            String content,
            boolean isDeleted,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return CommentResponse.builder()
                .commentId(commentId)
                .postId(postId)
                .postTitle(postTitle)
                .userId(userId)
                .nickname(nickname)
                .parentCommentId(parentCommentId)
                .content(content)
                .isDeleted(isDeleted)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .replies(new ArrayList<>())
                .attachments(new ArrayList<>())
                .build();
    }
}