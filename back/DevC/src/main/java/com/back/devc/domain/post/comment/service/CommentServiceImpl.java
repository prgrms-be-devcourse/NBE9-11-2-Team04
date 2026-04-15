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
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public CommentResponse createComment(Long postId, Long loginUserId, CommentCreateRequest request) {
        postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. id=" + postId));

        Comment comment = Comment.create(postId, loginUserId, null, request.getContent());
        Comment savedComment = commentRepository.save(comment);

        notificationService.createCommentNotification(postId, loginUserId, savedComment.getId());

        return toResponse(savedComment);
    }

    @Override
    @Transactional
    public CommentResponse createReply(Long parentCommentId, Long loginUserId, CommentCreateRequest request) {
        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new EntityNotFoundException("부모 댓글을 찾을 수 없습니다. id=" + parentCommentId));

        Comment reply = Comment.create(parentComment.getPostId(), loginUserId, parentCommentId, request.getContent());
        Comment savedReply = commentRepository.save(reply);

        notificationService.createReplyNotification(parentCommentId, loginUserId, savedReply.getId());

        return toResponse(savedReply);
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, Long loginUserId, CommentUpdateRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다. id=" + commentId));

        if (!comment.isOwner(loginUserId)) {
            throw new IllegalArgumentException("본인의 댓글만 수정할 수 있습니다.");
        }

        if (comment.isDeleted()) {
            throw new IllegalStateException("삭제된 댓글은 수정할 수 없습니다.");
        }

        comment.updateContent(request.getContent());
        return toResponse(comment);
    }

    @Override
    @Transactional
    public CommentDeleteResponse deleteComment(Long commentId, Long loginUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다. id=" + commentId));

        if (!comment.isOwner(loginUserId)) {
            throw new IllegalArgumentException("본인의 댓글만 삭제할 수 있습니다.");
        }

        if (!comment.isDeleted()) {
            comment.softDelete();
        }

        return new CommentDeleteResponse(comment.getId(), "댓글이 삭제되었습니다.");
    }

    @Override
    public CommentListResponse getComments(Long postId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. id=" + postId));

        List<CommentResponse> allComments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .stream()
                .map(this::toResponse)
                .toList();

        List<CommentResponse> parentComments = new java.util.ArrayList<>();
        java.util.Map<Long, CommentResponse> commentMap = new java.util.LinkedHashMap<>();

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

        return new CommentListResponse(parentComments);
    }

    private CommentResponse toResponse(Comment comment) {
        return CommentResponse.of(
                comment.getId(),
                comment.getPostId(),
                comment.getUserId(),
                null,
                comment.getParentCommentId(),
                comment.getContent(),
                comment.isDeleted(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}