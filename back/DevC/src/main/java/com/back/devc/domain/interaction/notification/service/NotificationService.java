package com.back.devc.domain.interaction.notification.service;

import com.back.devc.domain.interaction.notification.dto.NotificationListResponse;
import com.back.devc.domain.interaction.notification.dto.NotificationResponse;

public interface NotificationService {

    void createCommentNotification(Long postId, Long actorUserId, Long commentId);

    void createReplyNotification(Long parentCommentId, Long actorUserId, Long replyCommentId);

    void createPostLikeNotification(Long postId, Long actorUserId);

    /**
     * 게시글 북마크 알림 생성
     *
     * 북마크를 누른 사용자가 actorUserId,
     * 북마크된 게시글의 작성자가 알림 수신자가 됨
     */
    void createBookmarkNotification(Long postId, Long actorUserId);

    /**
     * 게시글 신고 알림 생성
     *
     * 신고를 누른 사용자가 actorUserId,
     * 신고된 게시글의 작성자가 알림 수신자가 됨
     */
    void createPostReportNotification(Long postId, Long actorUserId);

    /**
     * 댓글 신고 알림 생성
     *
     * 신고를 누른 사용자가 actorUserId,
     * 신고된 댓글의 작성자가 알림 수신자가 된됨
     */
    void createCommentReportNotification(Long commentId, Long actorUserId);

    void createReportNotification(Long targetUserId, Long actorUserId, Long postId, String message);

    NotificationListResponse getMyNotifications(Long loginUserId);

    NotificationResponse readNotification(Long notificationId, Long loginUserId);
}