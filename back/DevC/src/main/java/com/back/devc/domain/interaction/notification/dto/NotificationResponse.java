package com.back.devc.domain.interaction.notification.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long notificationId,
        Long userId,
        Long actorUserId,
        String actorNickname,
        Long postId,
        Long commentId,
        String type,
        String message,
        boolean isRead,
        LocalDateTime createdAt
) {

    public Long getNotificationId() {
        return notificationId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getActorUserId() {
        return actorUserId;
    }

    public String getActorNickname() {
        return actorNickname;
    }

    public Long getPostId() {
        return postId;
    }

    public Long getCommentId() {
        return commentId;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return isRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}