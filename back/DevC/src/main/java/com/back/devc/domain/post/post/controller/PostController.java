package com.back.devc.domain.post.post.controller;

import com.back.devc.domain.post.post.dto.*;
import com.back.devc.domain.post.post.entity.Post;
import com.back.devc.domain.post.post.service.PostService;
import com.back.devc.domain.post.post.type.PostSortType;
import com.back.devc.global.security.jwt.JwtPrincipal;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    @PostMapping
    public  ResponseEntity<PostCreateResponse> create(
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


    // 전체 목록 조회 -> 리스트로 담아서 전달 (관리자쪽에서 사용하기 위해 isDeleted=true인것도 같이 조회)
    @GetMapping("/admin")
    public ResponseEntity<List<AdminPostListResponse>> list() {

        List<Post> result = postService.findAll();

        List<AdminPostListResponse> postDtoList = result.stream()
                .map(AdminPostListResponse::new)
                .toList();

        return ResponseEntity.ok(postDtoList);
    }

    //상세 조회 하는 경우
    @GetMapping("/{postid}")
    public ResponseEntity<PostDetailResponse> detail(@PathVariable Long postid) {

        Post post = postService.findById(postid);

        return ResponseEntity.ok(PostDetailResponse.from(post));
    }

    //게시글 조회 (좋아요,최신순,조회수)
    //카테고리 파라미터를 넣으면 -> 카테고리 + (좋아요,최신순,조회수) 정렬가능하다
    @GetMapping
    public ResponseEntity<Page<PostListResponse>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "latest") PostSortType sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Page<Post> result = postService.getPosts(categoryId,sort, page, size);

        return ResponseEntity.ok(
                result.map(PostListResponse::new)
        );
    }

    //수정 하는 경우
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
            @PathVariable Long postId) {

        if (principal == null) {
            throw new RuntimeException("인증 정보가 없습니다.");
        }

        postService.delete(principal.userId(), postId);

        return ResponseEntity.ok(
                new PostDeleteResponse(postId, "삭제되었습니다.")
        );

    }
}