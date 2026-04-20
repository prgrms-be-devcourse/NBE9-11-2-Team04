package com.back.devc.domain.interaction.notification.dto;

import java.time.LocalDateTime;

public class NotificationResponse {

    private final Long notificationId;
    private final Long userId;
    private final Long actorUserId;
    private final String actorNickname;
    private final Long postId;
    private final Long commentId;
    private final String type;
    private final String message;
    private final boolean isRead;
    private final LocalDateTime createdAt;

    public NotificationResponse(
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
        this.notificationId = notificationId;
        this.userId = userId;
        this.actorUserId = actorUserId;
        this.actorNickname = actorNickname;
        this.postId = postId;
        this.commentId = commentId;
        this.type = type;
        this.message = message;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

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