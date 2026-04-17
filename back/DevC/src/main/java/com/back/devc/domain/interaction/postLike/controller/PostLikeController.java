package com.back.devc.domain.interaction.postLike.controller;

import com.back.devc.domain.interaction.postLike.dto.LikedPostResponse;
import com.back.devc.domain.interaction.postLike.dto.PostLikeResponse;
import com.back.devc.domain.interaction.postLike.service.PostLikeService;
import com.back.devc.global.security.jwt.JwtPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    /**
     * 게시글 좋아요 생성
     *
     * 현재 로그인한 사용자의 userId는 프론트에서 받지 않고,
     * SecurityContext에 들어있는 JwtPrincipal에서 꺼내 사용
     * 이렇게 해야 다른 사용자의 userId를 임의로 넣어 요청하는 것을 막을 수 있음
     */
    @PostMapping("/posts/{postId}/likes")
    public PostLikeResponse createLike(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long postId
    ) {
        return postLikeService.createLike(getAuthenticatedUserId(principal), postId);
    }

    /**
     * 게시글 좋아요 취소
     *
     * 좋아요 생성과 동일하게 현재 로그인한 사용자 기준으로만 처리
     */
    @DeleteMapping("/posts/{postId}/likes")
    public PostLikeResponse cancelLike(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long postId
    ) {
        return postLikeService.cancelLike(getAuthenticatedUserId(principal), postId);
    }

    /**
     * 내가 좋아요 누른 게시글 목록 조회
     *
     * 경로를 /users/me/likes 로 둔 이유는,
     * 특정 userId를 파라미터로 받는 대신 "현재 로그인한 사용자 본인" 기준 목록만 조회하도록 하기 위해서
     */
    @GetMapping("/users/me/likes")
    public List<LikedPostResponse> getLikedPosts(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        return postLikeService.getLikedPosts(getAuthenticatedUserId(principal));
    }

    /**
     * 컨트롤러에서 공통으로 사용하는 로그인 사용자 식별 메서드
     *
     * principal 이 없으면 비로그인 요청이므로 401을 반환하고,
     * 있으면 JwtPrincipal 안에 들어있는 userId를 꺼내 service로 넘김
     */
    private Long getAuthenticatedUserId(JwtPrincipal principal) {
        // 인증 없이 좋아요 관련 API를 호출한 경우 명확하게 401 UNAUTHORIZED 로 응답한다.
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }
        return principal.userId();
    }
}