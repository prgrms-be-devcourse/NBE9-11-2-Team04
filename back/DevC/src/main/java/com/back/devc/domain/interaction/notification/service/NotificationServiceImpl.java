package com.back.devc.domain.interaction.notification.service;

import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Override
    public void createCommentNotification(Long postId, Long actorUserId, Long commentId) {
        // TODO: Notification 엔티티/리포지토리 구현 후 저장 로직 추가
    }

    @Override
    public void createReplyNotification(Long parentCommentId, Long actorUserId, Long replyCommentId) {
        // TODO: Notification 엔티티/리포지토리 구현 후 저장 로직 추가
    }
}
