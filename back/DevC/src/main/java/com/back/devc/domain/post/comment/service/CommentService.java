package com.back.devc.domain.post.comment.service;

import com.back.devc.domain.post.comment.dto.CommentCreateRequest;
import com.back.devc.domain.post.comment.dto.CommentDeleteResponse;
import com.back.devc.domain.post.comment.dto.CommentListResponse;
import com.back.devc.domain.post.comment.dto.CommentResponse;
import com.back.devc.domain.post.comment.dto.CommentUpdateRequest;
import com.back.devc.domain.post.comment.entity.Comment;
import com.back.devc.domain.post.comment.repository.CommentRepository;
import com.back.devc.domain.post.comment.attachment.service.CommentAttachmentService;
import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.repository.MemberRepository;
import com.back.devc.domain.post.post.entity.Post;
import com.back.devc.domain.post.post.repository.PostRepository;
import com.back.devc.global.exception.ApiException;
import com.back.devc.global.exception.errorCode.CommentErrorCode;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.back.devc.domain.interaction.notification.service.NotificationService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final CommentAttachmentService commentAttachmentService;
    private final NotificationService notificationService;

    @Transactional
    public CommentResponse createComment(Long postId, Long loginUserId, CommentCreateRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(CommentErrorCode.COMMENT_404_POST_NOT_FOUND));

        Member member = memberRepository.findById(loginUserId)
                .orElseThrow(() -> new ApiException(CommentErrorCode.COMMENT_404_MEMBER_NOT_FOUND));

        Comment comment = Comment.create(
                postId,
                member.getUserId(),
                null,
                request.content()
        );
        Comment savedComment = commentRepository.save(comment);
        notificationService.createCommentNotification(postId, loginUserId, savedComment.getId());
        return toResponse(savedComment, post.getTitle(), member.getNickname());
    }

    @Transactional
    public CommentResponse createReply(Long parentCommentId, Long loginUserId, CommentCreateRequest request) {
        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new ApiException(CommentErrorCode.COMMENT_404_PARENT_NOT_FOUND));

        if (parentComment.isDeleted()) {
            throw new ApiException(CommentErrorCode.COMMENT_400_REPLY_TO_DELETED_COMMENT);
        }

        Post post = postRepository.findById(parentComment.getPostId())
                .orElseThrow(() -> new ApiException(CommentErrorCode.COMMENT_404_POST_NOT_FOUND));

        Member member = memberRepository.findById(loginUserId)
                .orElseThrow(() -> new ApiException(CommentErrorCode.COMMENT_404_MEMBER_NOT_FOUND));

        Comment reply = Comment.create(
                parentComment.getPostId(),
                member.getUserId(),
                parentComment.getId(),
                request.content()
        );
        Comment savedReply = commentRepository.save(reply);
        notificationService.createReplyNotification(parentCommentId, loginUserId, savedReply.getId());
        return toResponse(savedReply, post.getTitle(), member.getNickname());
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, Long loginUserId, CommentUpdateRequest request) {
        Comment comment = findComment(commentId);
        validateOwner(comment, loginUserId);

        comment.updateContent(request.content());

        String postTitle = findPostTitle(comment.getPostId());
        String nickname = findMemberNickname(comment.getUserId());

        return toResponse(comment, postTitle, nickname);
    }

    @Transactional
    public CommentDeleteResponse deleteComment(Long commentId, Long loginUserId) {
        Comment comment = findComment(commentId);
        validateOwner(comment, loginUserId);

        comment.softDelete();

        return new CommentDeleteResponse(commentId, "댓글 삭제 성공");
    }

    public CommentListResponse getComments(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. id=" + postId));

        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        List<CommentResponse> responses = comments.stream()
                .map(comment -> toResponse(comment, post.getTitle(), findMemberNickname(comment.getUserId())))
                .toList();

        return new CommentListResponse(buildCommentHierarchy(responses));
    }

    private List<CommentResponse> buildCommentHierarchy(List<CommentResponse> allComments) {
        Map<Long, CommentResponse> commentMap = new LinkedHashMap<>();
        List<CommentResponse> parentComments = new ArrayList<>();

        for (CommentResponse commentResponse : allComments) {
            commentMap.put(commentResponse.commentId(), commentResponse);
        }

        for (CommentResponse commentResponse : allComments) {
            if (commentResponse.parentCommentId() == null) {
                parentComments.add(commentResponse);
            } else {
                CommentResponse parent = commentMap.get(commentResponse.parentCommentId());
                if (parent != null) {
                    parent.replies().add(commentResponse);
                }
            }
        }

        return parentComments;
    }

    private CommentResponse toResponse(Comment comment, String postTitle, String nickname) {
        CommentResponse response = CommentResponse.of(
                comment.getId(),
                comment.getPostId(),
                postTitle,
                comment.getUserId(),
                nickname,
                comment.getParentCommentId(),
                comment.getContent(),
                comment.isDeleted(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );

        if (!comment.isDeleted()) {
            response.attachments().addAll(
                    commentAttachmentService.getAttachments(comment.getId()).attachments()
            );
        }

        return response;
    }

    private Comment findComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ApiException(CommentErrorCode.COMMENT_404_NOT_FOUND));
    }

    private void validateOwner(Comment comment, Long loginUserId) {
        if (!comment.getUserId().equals(loginUserId)) {
            throw new ApiException(CommentErrorCode.COMMENT_403_FORBIDDEN);
        }
    }

    private String findPostTitle(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(CommentErrorCode.COMMENT_404_POST_NOT_FOUND))
                .getTitle();
    }

    private String findMemberNickname(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new ApiException(CommentErrorCode.COMMENT_404_MEMBER_NOT_FOUND))
                .getNickname();
    }
}
