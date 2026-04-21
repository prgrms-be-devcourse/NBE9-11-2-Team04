package com.back.devc.domain.interaction.postLike.controller;

import com.back.devc.domain.interaction.postLike.dto.LikedPostResponse;
import com.back.devc.domain.interaction.postLike.dto.PostLikeResponse;
import com.back.devc.domain.interaction.postLike.service.PostLikeService;
import com.back.devc.global.response.SuccessResponse;
import com.back.devc.global.security.jwt.JwtPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.back.devc.domain.interaction.postLike.code.PostLikeSuccessCode.POSTLIKE_200_DELETE;
import static com.back.devc.domain.interaction.postLike.code.PostLikeSuccessCode.POSTLIKE_200_READ_LIST;
import static com.back.devc.domain.interaction.postLike.code.PostLikeSuccessCode.POSTLIKE_201_CREATE;
import static com.back.devc.global.security.jwt.JwtPrincipalHelper.getAuthenticatedUserId;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    /**
     * 게시글 좋아요 생성 API
     *
     * - 인증된 사용자와 게시글 ID를 기반으로 좋아요를 생성한다.
     * - 비즈니스 로직은 Service 계층에 위임한다.
     * - 성공 응답은 SuccessResponse 형식으로 통일한다.
     */
    @PostMapping("/posts/{postId}/likes")
    public ResponseEntity<SuccessResponse<PostLikeResponse>> createLike(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long postId
    ) {
        // JWT에서 인증된 사용자 ID를 추출한다.
        Long userId = getAuthenticatedUserId(principal);

        // 좋아요 생성 비즈니스 로직을 수행한다.
        PostLikeResponse response = postLikeService.createLike(userId, postId);

        // SuccessCode 기준으로 HTTP 상태 코드와 응답 본문을 일관되게 반환한다.
        return ResponseEntity
                .status(POSTLIKE_201_CREATE.getStatus())
                .body(SuccessResponse.of(POSTLIKE_201_CREATE, response));
    }

    /**
     * 게시글 좋아요 취소 API
     *
     * - 인증된 사용자의 좋아요를 삭제한다.
     * - 비즈니스 로직은 Service 계층에 위임한다.
     * - 성공 응답은 SuccessResponse 형식으로 통일한다.
     */
    @DeleteMapping("/posts/{postId}/likes")
    public ResponseEntity<SuccessResponse<PostLikeResponse>> cancelLike(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long postId
    ) {
        // JWT에서 인증된 사용자 ID를 추출한다.
        Long userId = getAuthenticatedUserId(principal);

        // 좋아요 취소 비즈니스 로직을 수행한다.
        PostLikeResponse response = postLikeService.cancelLike(userId, postId);

        // SuccessCode 기준으로 HTTP 상태 코드와 응답 본문을 일관되게 반환한다.
        return ResponseEntity
                .status(POSTLIKE_200_DELETE.getStatus())
                .body(SuccessResponse.of(POSTLIKE_200_DELETE, response));
    }

    /**
     * 사용자가 좋아요한 게시글 목록 조회 API
     *
     * - 인증된 사용자의 좋아요 게시글 목록을 조회한다.
     * - 엔티티를 직접 노출하지 않고 응답 DTO 목록으로 반환한다.
     * - 성공 응답은 SuccessResponse 형식으로 통일한다.
     */
    @GetMapping("/users/me/likes")
    public ResponseEntity<SuccessResponse<List<LikedPostResponse>>> getLikedPosts(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        // JWT에서 인증된 사용자 ID를 추출한다.
        Long userId = getAuthenticatedUserId(principal);

        // 사용자가 좋아요한 게시글 목록을 조회한다.
        List<LikedPostResponse> response = postLikeService.getLikedPosts(userId);

        // SuccessCode 기준으로 HTTP 상태 코드와 응답 본문을 일관되게 반환한다.
        return ResponseEntity
                .status(POSTLIKE_200_READ_LIST.getStatus())
                .body(SuccessResponse.of(POSTLIKE_200_READ_LIST, response));
    }
}