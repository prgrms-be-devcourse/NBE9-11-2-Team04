package com.back.devc.domain.interaction.postLike.controller;

import com.back.devc.domain.interaction.postLike.dto.LikedPostResponse;
import com.back.devc.domain.interaction.postLike.dto.LikedPostsQuery;
import com.back.devc.domain.interaction.postLike.dto.PostLikeCommand;
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

    /**
     * 게시글 좋아요 추가
     */
    @PostMapping("/posts/{postId}/likes")
    public PostLikeResponse createLike(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long postId
    ) {
        PostLikeCommand command = PostLikeCommand.builder()
                .userId(getAuthenticatedUserId(principal))
                .postId(postId)
                .build();

        return postLikeService.createLike(command);
    }

    /**
     * 게시글 좋아요 취소
     */
    @DeleteMapping("/posts/{postId}/likes")
    public PostLikeResponse cancelLike(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long postId
    ) {
        PostLikeCommand command = PostLikeCommand.builder()
                .userId(getAuthenticatedUserId(principal))
                .postId(postId)
                .build();

        return postLikeService.cancelLike(command);
    }

    /**
     * 내가 좋아요한 게시글 목록 조회
     */
    @GetMapping("/users/me/likes")
    public List<LikedPostResponse> getLikedPosts(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        LikedPostsQuery query = LikedPostsQuery.builder()
                .userId(getAuthenticatedUserId(principal))
                .build();

        return postLikeService.getLikedPosts(query);
    }
}