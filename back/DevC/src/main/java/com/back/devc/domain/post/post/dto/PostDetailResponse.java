package com.back.devc.domain.post.post.dto;

import com.back.devc.domain.member.member.util.MemberDisplayUtil;
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
        boolean bookmarked,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PostDetailResponse from(Post post) {
        return from(post, false, false);
    }

    public static PostDetailResponse from(Post post, boolean liked, boolean bookmarked) {
        return new PostDetailResponse(
                post.getPostId(),
                post.getTitle(),
                post.getContent(),
                post.getMember() != null ? post.getMember().getUserId() : null,
                MemberDisplayUtil.getDisplayName(post.getMember()),
                post.getCategory().getCategoryId(),
                post.getViewCount(),
                post.getLikeCount(),
                post.getCommentCount(),
                liked,
                bookmarked,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}