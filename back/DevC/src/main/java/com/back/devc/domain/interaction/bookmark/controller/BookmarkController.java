package com.back.devc.domain.interaction.bookmark.controller;

import com.back.devc.domain.interaction.bookmark.dto.BookmarkResponse;
import com.back.devc.domain.interaction.bookmark.service.BookmarkService;
import com.back.devc.global.response.SuccessResponse;
import com.back.devc.global.security.jwt.JwtPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.back.devc.domain.interaction.bookmark.code.BookmarkSuccessCode.BOOKMARK_200_DELETE;
import static com.back.devc.domain.interaction.bookmark.code.BookmarkSuccessCode.BOOKMARK_201_CREATE;
import static com.back.devc.global.security.jwt.JwtPrincipalHelper.getAuthenticatedUserId;

/**
 * 북마크 관련 API 컨트롤러
 *
 * - 북마크 생성 및 취소 기능을 제공한다.
 * - 인증된 사용자 정보를 기반으로 동작한다.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookmarkController {

    // 북마크 비즈니스 로직을 처리하는 서비스
    private final BookmarkService bookmarkService;

    /**
     * 북마크 생성 API
     *
     * [POST] /api/posts/{postId}/bookmarks
     *
     * - 특정 게시글(postId)에 대해 북마크를 생성한다.
     * - JWT에서 인증된 사용자 ID를 추출하여 처리한다.
     * - 성공 시 BOOKMARK_201_CREATE 코드와 함께 결과를 반환한다.
     */
    @PostMapping("/posts/{postId}/bookmarks")
    public ResponseEntity<SuccessResponse<BookmarkResponse>> createBookmark(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long postId
    ) {
        // JWT에서 인증된 사용자 ID 추출
        Long userId = getAuthenticatedUserId(principal);

        // 북마크 생성 서비스 호출
        BookmarkResponse response = bookmarkService.createBookmark(userId, postId);

        // 성공 응답 반환
        return ResponseEntity
                .status(BOOKMARK_201_CREATE.getStatus())
                .body(SuccessResponse.of(BOOKMARK_201_CREATE, response));
    }

    /**
     * 북마크 취소 API
     *
     * [DELETE] /api/posts/{postId}/bookmarks
     *
     * - 특정 게시글(postId)에 대한 북마크를 취소한다.
     * - JWT에서 인증된 사용자 ID를 기반으로 해당 사용자의 북마크만 삭제한다.
     * - 성공 시 BOOKMARK_200_DELETE 코드와 함께 결과를 반환한다.
     */
    @DeleteMapping("/posts/{postId}/bookmarks")
    public ResponseEntity<SuccessResponse<BookmarkResponse>> cancelBookmark(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long postId
    ) {
        // JWT에서 인증된 사용자 ID 추출
        Long userId = getAuthenticatedUserId(principal);

        // 북마크 취소 서비스 호출
        BookmarkResponse response = bookmarkService.cancelBookmark(userId, postId);

        // 성공 응답 반환
        return ResponseEntity
                .status(BOOKMARK_200_DELETE.getStatus())
                .body(SuccessResponse.of(BOOKMARK_200_DELETE, response));
    }
}