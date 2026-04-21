package com.back.devc.domain.interaction.bookmark.dto;

import com.back.devc.domain.post.post.entity.Post;
import com.back.devc.domain.member.member.util.MemberDisplayUtil;

import java.time.LocalDateTime;

/**
 * 북마크된 게시글 응답 DTO
 *
 * - 엔티티(Post)를 외부로 노출하지 않기 위해 DTO로 변환한다.
 * - 변환 책임을 DTO 내부로 이동시켜 서비스 계층의 의존성을 줄인다.
 */
public record BookmarkedPostResponse(
        long postId,
        String title,
        String authorNickname,
        long likeCount,
        long commentCount,
        LocalDateTime createdAt
) {

    /**
     * Post 엔티티를 기반으로 응답 DTO를 생성한다.
     */
    public static BookmarkedPostResponse from(Post post) {
        return new BookmarkedPostResponse(
                post.getPostId(),
                post.getTitle(),
                MemberDisplayUtil.getDisplayName(post.getMember()),
                post.getLikeCount(),
                post.getCommentCount(),
                post.getCreatedAt()
        );
    }
}