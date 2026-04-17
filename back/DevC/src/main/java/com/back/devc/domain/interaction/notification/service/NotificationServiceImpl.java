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
/**
 * 알림 비즈니스 로직 구현체
 *
 * 이 서비스는 "누가(receiver) 어떤 행동(actor)을 알림으로 받아야 하는지"를 결정하고,
 * 그 결과를 Notification 엔티티로 저장하는 역할
 *
 * 현재 이 프로젝트에서 다루는 알림 종류
 * - COMMENT : 내 게시글에 다른 사용자가 댓글을 남긴 경우
 * - REPLY   : 내 댓글에 다른 사용자가 답글을 남긴 경우
 * - LIKE    : 내 게시글에 다른 사용자가 좋아요를 누른 경우
 * - REPORT  : 내 게시글/댓글이 신고된 경우
 *
 * 구현 시 주의한 점
 * - 자기 자신이 한 행동은 알림을 만들지 않음
 * - soft delete 된 부모 댓글에는 답글 알림을 만들지 않음
 * - 좋아요 알림은 취소 후 다시 눌렀을 때 중복 생성되지 않도록 한 번만 만듦
 */
public class NotificationServiceImpl implements NotificationService {

    // 알림 저장/조회에 사용하는 JPA Repository.
    private final NotificationRepository notificationRepository;
    // 답글 알림 생성 시 부모 댓글 상태(존재 여부, 삭제 여부, 작성자)를 확인할 때 사용
    private final CommentRepository commentRepository;
    // 게시글 작성자(userId)를 찾아 "알림 수신자"를 결정할 때 사용
    private final PostRepository postRepository;

    /**
     * 게시글 댓글 알림 생성
     *
     * 흐름
     * 1) 게시글 작성자(userId)를 찾음
     * 2) 댓글 작성자가 본인 자신이면 알림을 만들지 않음
     * 3) 게시글 작성자를 receiver, 댓글 작성자를 actor로 하여 COMMENT 알림을 저장
     */
    @Override
    @Transactional
    public void createCommentNotification(Long postId, Long actorUserId, Long commentId) {
        Long postOwnerId = findPostOwnerId(postId);

        if (postOwnerId.equals(actorUserId)) {
            return;
        }

        saveNotification(
                postOwnerId,
                actorUserId,
                postId,
                commentId,
                "COMMENT",
                actorUserId + "번 사용자가 게시글에 댓글을 남겼습니다."
        );
    }

    /**
     * 대댓글(답글) 알림 생성
     *
     * 흐름
     * 1) 부모 댓글을 조회
     * 2) 부모 댓글이 soft delete 상태면 알림을 만들지 않음
     * 3) 부모 댓글 작성자를 receiver로 잡음
     * 4) 답글 작성자가 본인 자신이면 알림을 만들지 않음
     * 5) 부모 댓글 작성자에게 REPLY 알림을 저장
     */
    @Override
    @Transactional
    public void createReplyNotification(Long parentCommentId, Long actorUserId, Long replyCommentId) {
        Comment parentComment = findCommentOrThrow(parentCommentId, "부모 댓글을 찾을 수 없습니다. id=" + parentCommentId);

        if (parentComment.isDeleted()) {
            return;
        }

        Long receiverUserId = parentComment.getUserId();

        if (receiverUserId.equals(actorUserId)) {
            return;
        }

        saveNotification(
                receiverUserId,
                actorUserId,
                parentComment.getPostId(),
                replyCommentId,
                "REPLY",
                actorUserId + "번 사용자가 회원님의 댓글에 답글을 남겼습니다."
        );
    }

    /**
     * 게시글 좋아요 알림 생성
     *
     * 주의 사항
     * - 자기 자신의 게시글에 좋아요를 누른 경우 알림을 만들지 않음
     * - 같은 사용자가 같은 게시글에 대해 좋아요 취소 후 다시 눌러도
     *   LIKE 알림은 한 번만 남기도록 중복 생성 방지 검사를 수행
     */
    @Override
    @Transactional
    public void createPostLikeNotification(Long postId, Long actorUserId) {
        Long postOwnerId = findPostOwnerId(postId);

        if (postOwnerId.equals(actorUserId)) {
            return;
        }

        boolean alreadyNotified = notificationRepository
                .existsByUserIdAndActorUserIdAndPostIdAndType(postOwnerId, actorUserId, postId, "LIKE");

        if (alreadyNotified) {
            return;
        }

        saveNotification(
                postOwnerId,
                actorUserId,
                postId,
                null,
                "LIKE",
                actorUserId + "번 사용자가 회원님의 게시글을 좋아합니다."
        );
    }

    /**
     * 신고 알림 생성
     *
     * 신고 기능은 게시글 신고/댓글 신고 두 흐름에서 공통으로 사용하므로,
     * receiver(targetUserId), actor(actorUserId), message를 외부에서 받아 공통 저장만 수행
     */
    @Override
    @Transactional
    public void createReportNotification(Long targetUserId, Long actorUserId, Long postId, String message) {
        if (targetUserId.equals(actorUserId)) {
            return;
        }

        saveNotification(
                targetUserId,
                actorUserId,
                postId,
                null,
                "REPORT",
                message
        );
    }

    /**
     * 현재 로그인한 사용자의 알림 목록 조회
     *
     * 최신 알림이 위로 오도록 createdAt 내림차순으로 조회한 뒤,
     * 프론트 응답용 NotificationResponse로 변환해서 반환
     */
    @Override
    public NotificationListResponse getMyNotifications(Long loginUserId) {
        List<NotificationResponse> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(loginUserId)
                .stream()
                .map(this::toResponse)
                .toList();

        return new NotificationListResponse(notifications);
    }

    /**
     * 특정 알림 읽음 처리
     *
     * 본인 알림만 읽음 처리할 수 있도록 receiver(userId)를 한 번 더 검증
     * 즉, notificationId만 안다고 해서 다른 사용자의 알림을 읽음 처리할 수는 없음
     */
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

    // 게시글 작성자를 조회해 알림 수신자(receiver)를 구하는 공통 메서드
    private Long findPostOwnerId(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. id=" + postId))
                .getMember()
                .getUserId();
    }

    // 댓글 조회 공통 메서드. 답글 알림 생성 시 부모 댓글 검증에 사용
    private Comment findCommentOrThrow(Long commentId, String message) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(message));
    }

    /**
     * 알림 공통 저장 메서드
     *
     * 각 알림 생성 메서드는 receiver / actor / type / message를 결정하는 역할에 집중하고,
     * 실제 Notification 엔티티 생성 및 저장은 여기서 공통 처리
     */
    private void saveNotification(
            Long receiverUserId,
            Long actorUserId,
            Long postId,
            Long commentId,
            String type,
            String message
    ) {
        Notification notification = Notification.create(
                receiverUserId,
                actorUserId,
                postId,
                commentId,
                type,
                message
        );

        notificationRepository.save(notification);
    }

    // Entity -> Response DTO 변환 메서드
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
