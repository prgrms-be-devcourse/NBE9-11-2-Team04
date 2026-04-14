package com.back.devc.domain.post.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentDeleteResponse {

    private Long commentId;
    private String message;
}