package com.back.devc.domain.post.comment.service;

import com.back.devc.domain.post.comment.dto.*;

public interface CommentService {

    CommentResponse createComment(Long postId, Long loginUserId, CommentCreateRequest request);

    CommentResponse createReply(Long parentCommentId, Long loginUserId, CommentCreateRequest request);

    CommentResponse updateComment(Long commentId, Long loginUserId, CommentUpdateRequest request);

    CommentDeleteResponse deleteComment(Long commentId, Long loginUserId);

    CommentListResponse getComments(Long postId);
}