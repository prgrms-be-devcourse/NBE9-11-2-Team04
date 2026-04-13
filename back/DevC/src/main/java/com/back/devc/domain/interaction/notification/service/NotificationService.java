package com.back.devc.domain.interaction.notification.service;

public interface NotificationService {

    void createCommentNotification(Long postId, Long actorUserId, Long commentId);

    void createReplyNotification(Long parentCommentId, Long actorUserId, Long replyCommentId);
}
