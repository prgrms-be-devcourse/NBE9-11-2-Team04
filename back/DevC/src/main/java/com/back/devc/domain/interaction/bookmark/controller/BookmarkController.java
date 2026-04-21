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

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    /**
     * 북마크 생성 API
     *
     * - 인증된 사용자 ID와 게시글 ID를 기반으로 북마크를 생성한다.
     * - 비즈니스 로직은 Service 계층에 위임한다.
     * - 성공 응답은 SuccessResponse 형식으로 통일한다.
     */
    @PostMapping("/posts/{postId}/bookmarks")
    public ResponseEntity<SuccessResponse<BookmarkResponse>> createBookmark(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long postId
    ) {
        // JWT에서 인증된 사용자 ID를 추출한다.
        Long userId = getAuthenticatedUserId(principal);

        // 북마크 생성 비즈니스 로직을 수행한다.
        BookmarkResponse response = bookmarkService.createBookmark(userId, postId);

        // SuccessCode 기준으로 HTTP 상태 코드와 응답 본문을 일관되게 반환한다.
        return ResponseEntity
                .status(BOOKMARK_201_CREATE.getStatus())
                .body(SuccessResponse.of(BOOKMARK_201_CREATE, response));
    }

    /**
     * 북마크 취소 API
     *
     * - 인증된 사용자의 북마크를 삭제한다.
     * - 비즈니스 로직은 Service 계층에 위임한다.
     * - 성공 응답은 SuccessResponse 형식으로 통일한다.
     */
    @DeleteMapping("/posts/{postId}/bookmarks")
    public ResponseEntity<SuccessResponse<BookmarkResponse>> cancelBookmark(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long postId
    ) {
        // JWT에서 인증된 사용자 ID를 추출한다.
        Long userId = getAuthenticatedUserId(principal);

        // 북마크 취소 비즈니스 로직을 수행한다.
        BookmarkResponse response = bookmarkService.cancelBookmark(userId, postId);

        // SuccessCode 기준으로 HTTP 상태 코드와 응답 본문을 일관되게 반환한다.
        return ResponseEntity
                .status(BOOKMARK_200_DELETE.getStatus())
                .body(SuccessResponse.of(BOOKMARK_200_DELETE, response));
    }
}