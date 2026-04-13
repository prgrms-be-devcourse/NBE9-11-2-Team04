package com.back.devc.domain.interaction.bookmark.controller;

import com.back.devc.domain.interaction.bookmark.dto.BookmarkResponse;
import com.back.devc.domain.interaction.bookmark.dto.BookmarkedPostResponse;
import com.back.devc.domain.interaction.bookmark.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping("/posts/{postId}/bookmarks")
    public BookmarkResponse createBookmark(
            @RequestParam int userId,
            @PathVariable int postId
    ) {
        return bookmarkService.createBookmark(userId, postId);
    }

    @DeleteMapping("/posts/{postId}/bookmarks")
    public BookmarkResponse cancelBookmark(
            @RequestParam int userId,
            @PathVariable int postId
    ) {
        return bookmarkService.cancelBookmark(userId, postId);
    }

    @GetMapping("/users/me/bookmarks")
    public List<BookmarkedPostResponse> getBookmarkedPosts(@RequestParam int userId) {
        return bookmarkService.getBookmarkedPosts(userId);
    }
}