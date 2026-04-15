package com.back.devc.domain.interaction.notification.service;

import com.back.devc.domain.interaction.notification.dto.NotificationListResponse;
import com.back.devc.domain.interaction.notification.dto.NotificationResponse;
import com.back.devc.domain.interaction.notification.entity.Notification;
import com.back.devc.domain.interaction.notification.repository.NotificationRepository;
import com.back.devc.domain.post.comment.entity.Comment;
import com.back.devc.domain.post.comment.repository.CommentRepository;
import com.back.devc.domain.post.post.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Override
    @Transactional
    public void createCommentNotification(Long postId, Long actorUserId, Long commentId) {
        Long postOwnerId = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. id=" + postId))
                .getMember()
                .getUserId();

        if (postOwnerId.equals(actorUserId)) {
            return;
        }

        Notification notification = Notification.create(
                postOwnerId,
                actorUserId,
                postId,
                commentId,
                "COMMENT",
                actorUserId + "번 사용자가 게시글에 댓글을 남겼습니다."
        );

        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void createReplyNotification(Long parentCommentId, Long actorUserId, Long replyCommentId) {
        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new EntityNotFoundException("부모 댓글을 찾을 수 없습니다. id=" + parentCommentId));

        if (parentComment.isDeleted()) {
            return;
        }

        Long receiverUserId = parentComment.getUserId();

        if (receiverUserId.equals(actorUserId)) {
            return;
        }

        Notification notification = Notification.create(
                receiverUserId,
                actorUserId,
                parentComment.getPostId(),
                replyCommentId,
                "REPLY",
                actorUserId + "번 사용자가 회원님의 댓글에 답글을 남겼습니다."
        );

        notificationRepository.save(notification);
    }

    @Override
    public NotificationListResponse getMyNotifications(Long loginUserId) {
        List<NotificationResponse> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(loginUserId)
                .stream()
                .map(this::toResponse)
                .toList();

        return new NotificationListResponse(notifications);
    }

    @Override
    @Transactional
    public NotificationResponse readNotification(Long notificationId, Long loginUserId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("알림을 찾을 수 없습니다. id=" + notificationId));

        if (!notification.getUserId().equals(loginUserId)) {
            throw new IllegalArgumentException("본인의 알림만 읽음 처리할 수 있습니다.");
        }

        notification.markAsRead();
        return toResponse(notification);
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUserId(),
                notification.getActorUserId(),
                notification.getPostId(),
                notification.getCommentId(),
                notification.getType(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
