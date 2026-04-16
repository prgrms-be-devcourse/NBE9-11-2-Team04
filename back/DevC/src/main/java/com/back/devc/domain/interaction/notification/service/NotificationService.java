package com.back.devc.domain.interaction.notification.service;

import com.back.devc.domain.interaction.notification.dto.NotificationListResponse;
import com.back.devc.domain.interaction.notification.dto.NotificationResponse;

public interface NotificationService {

    void createCommentNotification(Long postId, Long actorUserId, Long commentId);

    void createReplyNotification(Long parentCommentId, Long actorUserId, Long replyCommentId);

    void createPostLikeNotification(Long postId, Long actorUserId);

    void createReportNotification(Long targetUserId, Long actorUserId, Long postId, String message);

    NotificationListResponse getMyNotifications(Long loginUserId);

    NotificationResponse readNotification(Long notificationId, Long loginUserId);
}