package com.back.devc.domain.interaction.bookmark.controller;

import com.back.devc.domain.interaction.bookmark.dto.BookmarkResponse;
import com.back.devc.domain.interaction.bookmark.service.BookmarkService;
import com.back.devc.global.security.jwt.JwtPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping("/posts/{postId}/bookmarks")
    public BookmarkResponse createBookmark(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long postId
    ) {
        return bookmarkService.createBookmark(principal.userId(), postId);
    }

    @DeleteMapping("/posts/{postId}/bookmarks")
    public BookmarkResponse cancelBookmark(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long postId
    ) {
        return bookmarkService.cancelBookmark(principal.userId(), postId);
    }
}