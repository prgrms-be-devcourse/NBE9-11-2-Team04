package com.back.devc.domain.post.post.dto;

import com.back.devc.domain.member.member.util.MemberDisplayUtil;
import com.back.devc.domain.post.post.entity.Post;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record PostListResponse(
        Long postId,
        String title,
        String content,
        Long userId,
        String nickName,
        Long categoryId,
        int viewCount,
        int likeCount,
        int commentCount,
        boolean liked,
        boolean bookmarked,
        LocalDateTime createdAt
) {
    public static PostListResponse from(Post post, boolean liked, boolean bookmarked) {
        return PostListResponse.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .content(post.getContent())
                .userId(post.getMember() != null ? post.getMember().getUserId() : null)
                .nickName(MemberDisplayUtil.getDisplayName(post.getMember()))
                .categoryId(post.getCategory().getCategoryId())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .liked(liked)
                .bookmarked(bookmarked)
                .createdAt(post.getCreatedAt())
                .build();
    }
}