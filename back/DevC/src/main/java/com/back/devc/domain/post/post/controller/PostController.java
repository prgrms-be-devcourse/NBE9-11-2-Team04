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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
        if (principal == null) {
            throw new RuntimeException("인증 정보가 없습니다.");
        }

        Post post = postService.write(
                principal.userId(),
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
        Long loginUserId = principal != null ? principal.userId() : null;
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
        if (principal == null) {
            throw new RuntimeException("인증 정보가 없습니다.");
        }

        Post post = postService.update(
                principal.userId(),
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
            @PathVariable Long postId
    ) {
        if (principal == null) {
            throw new RuntimeException("인증 정보가 없습니다.");
        }

        postService.delete(principal.userId(), postId);

        return ResponseEntity.ok(new PostDeleteResponse(postId, "삭제되었습니다."));
    }
}