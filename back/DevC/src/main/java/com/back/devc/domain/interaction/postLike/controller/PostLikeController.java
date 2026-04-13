package com.back.devc.domain.interaction.postLike.controller;

import com.back.devc.domain.interaction.postLike.dto.LikedPostResponse;
import com.back.devc.domain.interaction.postLike.dto.PostLikeResponse;
import com.back.devc.domain.interaction.postLike.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping("/posts/{postId}/likes")
    public PostLikeResponse createLike(
            @RequestParam int userId,
            @PathVariable int postId
    ) {
        return postLikeService.createLike(userId, postId);
    }

    @DeleteMapping("/posts/{postId}/likes")
    public PostLikeResponse cancelLike(
            @RequestParam int userId,
            @PathVariable int postId
    ) {
        return postLikeService.cancelLike(userId, postId);
    }

    @GetMapping("/users/me/likes")
    public List<LikedPostResponse> getLikedPosts(@RequestParam int userId) {
        return postLikeService.getLikedPosts(userId);
    }
}