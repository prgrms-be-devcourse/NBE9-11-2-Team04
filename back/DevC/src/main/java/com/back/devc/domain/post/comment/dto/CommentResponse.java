package com.back.devc.domain.post.comment.dto;

import com.back.devc.domain.post.comment.attachment.dto.CommentAttachmentResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CommentResponse {

    private Long commentId;
    private Long postId;
    private Long userId;
    private String nickname;
    private Long parentCommentId;
    private String content;
    private boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CommentResponse> replies;
    private List<CommentAttachmentResponse> attachments;

    public static CommentResponse of(
            Long commentId,
            Long postId,
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