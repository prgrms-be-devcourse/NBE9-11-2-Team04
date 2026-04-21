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
import com.back.devc.global.response.SuccessCode;
import com.back.devc.global.response.SuccessResponse;

import static com.back.devc.global.security.jwt.JwtPrincipalHelper.getAuthenticatedUserId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    // 게시글 생성
    @PostMapping
    public ResponseEntity<SuccessResponse<PostCreateResponse>> create(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestBody @Valid PostCreateRequest request
    ) {
        Post post = postService.write(
                getAuthenticatedUserId(principal),
                request.categoryId(),
                request.title(),
                request.content()
        );

        PostCreateResponse response = PostCreateResponse.from(post);
        SuccessCode successCode = SuccessCode.POST_CREATE_SUCCESS;

        return ResponseEntity
                .status(successCode.getStatus())
                .body(SuccessResponse.of(successCode, response));

    }

    //게시글 상세조회
    @GetMapping("/{postid}")
    public ResponseEntity<PostDetailResponse> detail(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long postid
    ) {
        Long loginUserId = principal != null ? principal.userId() : null;
        return ResponseEntity.ok(postService.findDetailById(postid, loginUserId));
    }


    //게시글 목록조회
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
        Long loginUserId = principal != null ? getAuthenticatedUserId(principal) : null;

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

    //게시글 수정
    @PutMapping("/{postId}")
    public ResponseEntity<SuccessResponse<PostUpdateResponse>> update(
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

        PostUpdateResponse response = PostUpdateResponse.from(post);
        SuccessCode successCode = SuccessCode.POST_UPDATE_SUCCESS;

        return ResponseEntity
                .status(successCode.getStatus())
                .body(SuccessResponse.of(successCode, response));
    }

    //게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<PostDeleteResponse> delete(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long postId
    ) {
        postService.delete(getAuthenticatedUserId(principal), postId);

        return ResponseEntity.ok(new PostDeleteResponse(postId, "삭제되었습니다."));
    }
}