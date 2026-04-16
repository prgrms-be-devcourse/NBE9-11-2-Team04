package com.back.devc.domain.post.post.dto;

import com.back.devc.domain.post.post.entity.Post;

import java.time.LocalDateTime;

public record PostDetailResponse(
        Long postId,
        String title,
        String content,
        Long userId,
        String writerName,
        Long categoryId,
        int viewCount,
        int likeCount,
        int commentCount,
        boolean liked,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PostDetailResponse from(Post post) {
        return from(post, false);
    }

    public static PostDetailResponse from(Post post, boolean liked) {
        return new PostDetailResponse(
                post.getPostId(),
                post.getTitle(),
                post.getContent(),
                post.getMember() != null ? post.getMember().getUserId() : null,
                post.getMember() != null ? post.getMember().getNickname() : null,
                post.getCategory().getCategoryId(),
                post.getViewCount(),
                post.getLikeCount(),
                post.getCommentCount(),
                liked,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}