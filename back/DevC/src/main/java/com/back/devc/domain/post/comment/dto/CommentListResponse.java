package com.back.devc.domain.post.comment.dto;

import java.util.List;

public record CommentListResponse(
        List<CommentResponse> comments
) {

    public List<CommentResponse> getComments() {
        return comments;
    }
}