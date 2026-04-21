package com.back.devc.domain.post.comment.dto;

import com.back.devc.domain.post.comment.attachment.dto.CommentAttachmentResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
public record CommentResponse(
        Long commentId,
        Long postId,
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

    public Long getCommentId() {
        return commentId;
    }

    public Long getPostId() {
        return postId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getNickname() {
        return nickname;
    }

    public Long getParentCommentId() {
        return parentCommentId;
    }

    public String getContent() {
        return content;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<CommentResponse> getReplies() {
        return replies;
    }

    public List<CommentAttachmentResponse> getAttachments() {
        return attachments;
    }
@AllArgsConstructor
public class CommentResponse {

    private Long commentId;
    private Long postId;
    private String postTitle;
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