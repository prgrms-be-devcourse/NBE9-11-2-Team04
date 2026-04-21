package com.back.devc.domain.interaction.postLike.controller;

import com.back.devc.domain.interaction.postLike.dto.LikedPostResponse;
import com.back.devc.domain.interaction.postLike.dto.PostLikeResponse;
import com.back.devc.domain.interaction.postLike.service.PostLikeService;
import com.back.devc.global.security.jwt.JwtPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.back.devc.global.security.jwt.JwtPrincipalHelper.getAuthenticatedUserId;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping("/posts/{postId}/likes")
    public PostLikeResponse createLike(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long postId
    ) {
        return postLikeService.createLike(getAuthenticatedUserId(principal), postId);
    }

    @DeleteMapping("/posts/{postId}/likes")
    public PostLikeResponse cancelLike(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long postId
    ) {
        return postLikeService.cancelLike(getAuthenticatedUserId(principal), postId);
    }

    @GetMapping("/users/me/likes")
    public List<LikedPostResponse> getLikedPosts(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        return postLikeService.getLikedPosts(getAuthenticatedUserId(principal));
    }

}