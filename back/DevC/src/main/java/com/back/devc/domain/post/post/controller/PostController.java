package com.back.devc.domain.post.post.controller;

import com.back.devc.domain.post.post.dto.*;
import com.back.devc.domain.post.post.entity.Post;
import com.back.devc.domain.post.post.service.PostService;
import com.back.devc.domain.post.post.type.PostSearchType;
import com.back.devc.domain.post.post.type.PostSortType;
import com.back.devc.global.security.jwt.JwtPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostCreateResponse> create(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestBody @Valid PostCreateRequest request
    ) {
        Post post = postService.write(
                getAuthenticatedUserId(principal),
                request.categoryId(),
                request.title(),
                request.content()
        );

        return ResponseEntity.ok(PostCreateResponse.from(post));
    }

    @GetMapping("/{postid}")
    public ResponseEntity<PostDetailResponse> detail(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long postid
    ) {

        Long loginUserId = principal != null ? getAuthenticatedUserId(principal) : null;

        return ResponseEntity.ok(postService.findDetailById(postid, loginUserId));
    }

    @GetMapping
    public ResponseEntity<Page<PostListResponse>> list(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) PostSearchType searchType,
            @RequestParam(defaultValue = "LATEST") PostSortType sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long loginUserId = principal != null ? principal.userId() : null;

        Page<PostListResponse> result = postService.getPosts(
                loginUserId,
                categoryId,
                keyword,
                searchType,
                sort,
                page,
                size
        );

        return ResponseEntity.ok(result);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostUpdateResponse> update(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long postId,
            @RequestBody @Valid PostUpdateRequest postUpdateRequest
    ) {

        Post post = postService.update(
                getAuthenticatedUserId(principal),
                postId,
                postUpdateRequest.title(),
                postUpdateRequest.content(),
                postUpdateRequest.categoryId()
        );

        return ResponseEntity.ok(PostUpdateResponse.from(post));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<PostDeleteResponse> delete(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long postId) {

        postService.delete(getAuthenticatedUserId(principal), postId);

        return ResponseEntity.ok(new PostDeleteResponse(postId, "삭제되었습니다."));
    }

    /**
     * 게시글 컨트롤러에서 공통으로 사용하는 로그인 사용자 식별 메서드
     *
     * JwtAuthenticationFilter가 정상적으로 principal을 세팅한 경우 userId를 반환하고,
     * 인증 정보가 없으면 RuntimeException 대신 401 UNAUTHORIZED를 반환하도록 방어
     */
    private Long getAuthenticatedUserId(JwtPrincipal principal) {
        // 토큰이 없거나 필터에서 principal을 세팅하지 못한 요청은 인증 실패로 처리한다.
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }
        return principal.userId();
    }
}