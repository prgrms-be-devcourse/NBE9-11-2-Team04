package com.back.devc.domain.post.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CommentListResponse {

    private List<CommentResponse> comments;
}