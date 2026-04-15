package com.back.devc.domain.post.post.controller;

import com.back.devc.domain.post.post.dto.PostCreateRequest;
import com.back.devc.domain.post.post.dto.PostCreateResponse;
import com.back.devc.domain.post.post.dto.PostDeleteResponse;
import com.back.devc.domain.post.post.dto.PostDetailResponse;
import com.back.devc.domain.post.post.dto.PostListResponse;
import com.back.devc.domain.post.post.dto.PostUpdateRequest;
import com.back.devc.domain.post.post.dto.PostUpdateResponse;
import com.back.devc.domain.post.post.entity.Post;
import com.back.devc.domain.post.post.service.PostService;
import com.back.devc.global.security.jwt.JwtPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    @PostMapping
    public PostCreateResponse create(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestBody @Valid PostCreateRequest request
    ) {
        Post post = postService.write(
                principal.userId(),
                request.categoryId(),
                request.title(),
                request.content()
        );

        return PostCreateResponse.from(post);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PostListResponse> list() {
        List<Post> result = postService.findAll();

        return result.reversed().stream()
                .map(PostListResponse::new)
                .toList();
    }

    @GetMapping("/{postId}")
    public PostDetailResponse detail(@PathVariable Long postId) {
        Post post = postService.findById(postId);
        return PostDetailResponse.from(post);
    }

    @PutMapping("/{postId}")
    public PostUpdateResponse update(
            @PathVariable Long postId,
            @RequestBody @Valid PostUpdateRequest request
    ) {
        Post post = postService.update(
                postId,
                request.title(),
                request.content(),
                request.categoryId()
        );
        return PostUpdateResponse.from(post);
    }

    @DeleteMapping("/{postId}")
    public PostDeleteResponse delete(@PathVariable Long postId) {
        postService.delete(postId);
        return new PostDeleteResponse(postId, "삭제되었습니다.");
    }
}