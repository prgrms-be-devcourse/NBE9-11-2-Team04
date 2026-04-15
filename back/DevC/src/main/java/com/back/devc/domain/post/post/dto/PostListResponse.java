package com.back.devc.domain.post.post.dto;

import com.back.devc.domain.post.post.entity.Post;

import java.time.LocalDateTime;
import java.util.Optional;

//게시글 전체 조회로 간략한 정보만 담아서 전달
public record PostListResponse(
        Long postId,
        String title,
        String content,
        Long userId,
        Long categoryId,
        LocalDateTime createdAt
) {
    public PostListResponse(Post post) {
        this(
                post.getPostId(),
                post.getTitle(),
                post.getContent(),
                post.getMember() != null ? post.getMember().getUserId() : null,
                post.getCategory().getCategoryId(),
                post.getCreatedAt()
        );
    }
}