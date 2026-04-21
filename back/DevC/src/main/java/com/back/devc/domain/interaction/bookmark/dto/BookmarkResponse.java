package com.back.devc.domain.interaction.bookmark.dto;

/**
 * 북마크 상태 응답 DTO
 *
 * - 북마크 생성/취소 결과를 클라이언트에 전달한다.
 * - 상황별 응답을 정적 팩토리 메서드로 제공하여 생성 책임을 캡슐화한다.
 */
public record BookmarkResponse(
        long postId,
        boolean bookmarked,
        String message
) {

    public static BookmarkResponse created(Long postId) {
        return new BookmarkResponse(postId, true, "북마크가 추가되었습니다.");
    }

    public static BookmarkResponse alreadyBookmarked(Long postId) {
        return new BookmarkResponse(postId, true, "이미 북마크한 게시글입니다.");
    }

    public static BookmarkResponse canceled(Long postId) {
        return new BookmarkResponse(postId, false, "북마크가 취소되었습니다.");
    }

    public static BookmarkResponse alreadyCanceled(Long postId) {
        return new BookmarkResponse(postId, false, "북마크가 이미 취소된 상태입니다.");
    }
}