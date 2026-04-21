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

    @PostMapping("/posts/{postId}/bookmarks")
    public ResponseEntity<SuccessResponse<BookmarkResponse>> createBookmark(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long postId
    ) {
        Long userId = getAuthenticatedUserId(principal);
        BookmarkResponse response = bookmarkService.createBookmark(userId, postId);

        return ResponseEntity
                .status(BOOKMARK_201_CREATE.getStatus())
                .body(SuccessResponse.of(BOOKMARK_201_CREATE, response));
    }

    @DeleteMapping("/posts/{postId}/bookmarks")
    public ResponseEntity<SuccessResponse<BookmarkResponse>> cancelBookmark(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long postId
    ) {
        Long userId = getAuthenticatedUserId(principal);
        BookmarkResponse response = bookmarkService.cancelBookmark(userId, postId);

        return ResponseEntity
                .status(BOOKMARK_200_DELETE.getStatus())
                .body(SuccessResponse.of(BOOKMARK_200_DELETE, response));
    }
}