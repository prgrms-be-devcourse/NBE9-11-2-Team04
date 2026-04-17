package com.back.devc.domain.post.comment.service;

import com.back.devc.domain.interaction.notification.service.NotificationService;
import com.back.devc.domain.post.comment.dto.CommentCreateRequest;
import com.back.devc.domain.post.comment.dto.CommentDeleteResponse;
import com.back.devc.domain.post.comment.dto.CommentListResponse;
import com.back.devc.domain.post.comment.dto.CommentResponse;
import com.back.devc.domain.post.comment.dto.CommentUpdateRequest;
import com.back.devc.domain.post.comment.entity.Comment;
import com.back.devc.domain.post.comment.repository.CommentRepository;
import com.back.devc.domain.post.post.repository.PostRepository;
import com.back.devc.domain.member.member.repository.MemberRepository;
import com.back.devc.domain.post.comment.attachment.service.CommentAttachmentService;
import com.back.devc.domain.member.member.entity.Member;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    // 댓글 조회 응답에 첨부파일 목록도 함께 담기 위해 사용하는 서비스
    private final CommentAttachmentService commentAttachmentService;
    // 댓글/대댓글 작성 시 알림 기능과 연결하기 위해 주입한 서비스
    private final NotificationService notificationService;

    @Override
    @Transactional
    public CommentResponse createComment(Long postId, Long loginUserId, CommentCreateRequest request) {
        postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. id=" + postId));

        Comment comment = Comment.create(postId, loginUserId, null, request.getContent());
        Comment savedComment = commentRepository.save(comment);

        // 댓글 저장이 끝난 뒤 게시글 작성자에게 댓글 알림을 생성
        // 알림 생성은 service 내부에서 "자기 글에 본인이 댓글 단 경우 알림 제외" 같은 정책까지 함께 처리
        notificationService.createCommentNotification(postId, loginUserId, savedComment.getId());

        return toResponse(savedComment);
    }

    @Override
    @Transactional
    public CommentResponse createReply(Long parentCommentId, Long loginUserId, CommentCreateRequest request) {
        Comment parentComment = findCommentOrThrow(parentCommentId, "부모 댓글을 찾을 수 없습니다. id=" + parentCommentId);
        validateReplyWritable(parentComment, parentCommentId);

        Comment reply = Comment.create(parentComment.getPostId(), loginUserId, parentCommentId, request.getContent());
        Comment savedReply = commentRepository.save(reply);

        // 대댓글 저장이 끝난 뒤 부모 댓글 작성자에게 답글 알림을 생성
        // 실제 알림 생성 가능 여부(삭제된 부모 댓글인지, 본인 댓글인지 등)는 NotificationService 에서 한 번 더 검증
        notificationService.createReplyNotification(parentCommentId, loginUserId, savedReply.getId());

        return toResponse(savedReply);
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, Long loginUserId, CommentUpdateRequest request) {
        Comment comment = findCommentOrThrow(commentId, "댓글을 찾을 수 없습니다. id=" + commentId);
        validateCommentOwner(comment, loginUserId, "본인의 댓글만 수정할 수 있습니다.");
        validateCommentNotDeleted(comment, "삭제된 댓글은 수정할 수 없습니다.");

        comment.updateContent(request.getContent());
        return toResponse(comment);
    }

    @Override
    @Transactional
    public CommentDeleteResponse deleteComment(Long commentId, Long loginUserId) {
        Comment comment = findCommentOrThrow(commentId, "댓글을 찾을 수 없습니다. id=" + commentId);
        validateCommentOwner(comment, loginUserId, "본인의 댓글만 삭제할 수 있습니다.");

        if (!comment.isDeleted()) {
            comment.softDelete();
        }

        return new CommentDeleteResponse(comment.getId(), "댓글이 삭제되었습니다.");
    }

    @Override
    public CommentListResponse getComments(Long postId) {
        validatePostExists(postId);

        List<CommentResponse> allComments = loadComments(postId);
        List<CommentResponse> parentComments = buildCommentHierarchy(allComments);

        return new CommentListResponse(parentComments);
    }

    private void validatePostExists(Long postId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. id=" + postId));
    }

    private List<CommentResponse> loadComments(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private List<CommentResponse> buildCommentHierarchy(List<CommentResponse> allComments) {
        List<CommentResponse> parentComments = new ArrayList<>();
        Map<Long, CommentResponse> commentMap = new LinkedHashMap<>();

        for (CommentResponse commentResponse : allComments) {
            commentMap.put(commentResponse.getCommentId(), commentResponse);
        }

        for (CommentResponse commentResponse : allComments) {
            if (commentResponse.getParentCommentId() == null) {
                parentComments.add(commentResponse);
            } else {
                CommentResponse parent = commentMap.get(commentResponse.getParentCommentId());
                if (parent != null) {
                    parent.getReplies().add(commentResponse);
                }
            }
        }

        return parentComments;
    }

    /**
     * 댓글 조회 공통 메서드
     *
     * 댓글 수정/삭제/답글 작성 등 여러 곳에서 같은 조회 로직이 반복되기 때문에
     * 중복을 줄이기 위해 공통 메서드로 분리
     */
    private Comment findCommentOrThrow(Long commentId, String message) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(message));
    }

    /**
     * 답글 작성 가능 여부 검증
     *
     * 현재 댓글은 soft delete 방식이라서,
     * 삭제된 부모 댓글에는 새로운 대댓글을 달 수 없도록 막음
     */
    private void validateReplyWritable(Comment parentComment, Long parentCommentId) {
        if (parentComment.isDeleted()) {
            throw new IllegalStateException("삭제된 댓글에는 대댓글을 작성할 수 없습니다. id=" + parentCommentId);
        }
    }

    /**
     * 현재 로그인 사용자가 해당 댓글의 작성자인지 검증
     *
     * 댓글 수정/삭제는 본인 댓글에 대해서만 가능하므로 공통 검증으로 분리
     */
    private void validateCommentOwner(Comment comment, Long loginUserId, String message) {
        if (!comment.isOwner(loginUserId)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 댓글이 soft delete 상태가 아닌지 검증
     *
     * 현재 정책상 삭제된 댓글은 수정할 수 없으므로,
     * 수정 전에 별도로 상태를 확인
     */
    private void validateCommentNotDeleted(Comment comment, String message) {
        if (comment.isDeleted()) {
            throw new IllegalStateException(message);
        }
    }

    private String findNickname(Long userId) {
        return memberRepository.findById(userId)
                .map(Member::getNickname)
                .orElse(null);
    }

    private CommentResponse toResponse(Comment comment) {
        CommentResponse response = CommentResponse.of(
                comment.getId(),
                comment.getPostId(),
                comment.getUserId(),
                findNickname(comment.getUserId()),
                comment.getParentCommentId(),
                comment.getContent(),
                comment.isDeleted(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );

        // 삭제된 댓글은 현재 정책상 첨부파일을 노출하지 않고,
        // 삭제되지 않은 댓글만 첨부파일 목록을 함께 내려준다.
        if (!comment.isDeleted()) {
            response.getAttachments().addAll(
                    commentAttachmentService.getAttachments(comment.getId()).getAttachments()
            );
        }

        return response;
    }
}