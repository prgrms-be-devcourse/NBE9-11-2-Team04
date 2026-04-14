package com.back.devc.domain.post.post.controller;

import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.post.post.dto.*;
import com.back.devc.domain.post.post.entity.Post;
import com.back.devc.domain.post.post.service.PostService;
import com.back.devc.domain.post.post.type.PostSortType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    //게시글 생성 + 여기에 로그인한 member 불러오는것 (추가했는데 확인 필요)
    @PostMapping
    public PostCreateResponse create(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid PostCreateRequest request
    ) {
        Post post = postService.write(
                member,
                request.categoryId(),
                request.title(),
                request.content()
        );

        return PostCreateResponse.from(post);
    }

    // 전체 목록 조회 -> 리스트로 담아서 전달
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PostListResponse> list() {
        List<Post> result = postService.findAll();

        List<PostListResponse> postDtoList = result.reversed().stream()
                .map(PostListResponse::new)
                .toList();
        return postDtoList;
    }

    //상세 조회 하는 경우
    @GetMapping("/{postid}")
    public PostDetailResponse detail(@PathVariable Long postid) {

        Post post = postService.findById(postid);

        return PostDetailResponse.from(post);
    }

    @GetMapping
    public ResponseEntity<Page<PostListResponse>> list(
            @RequestParam(defaultValue = "latest") PostSortType sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Page<Post> result = postService.getPosts(sort, page, size);

        return ResponseEntity.ok(
                result.map(PostListResponse::new)
        );
    }

    //수정 하는 경우
    @PutMapping("/{postId}")
    public PostUpdateResponse update(@PathVariable Long postId, @RequestBody @Valid PostUpdateRequest postUpdateRequest) {
        Post post =postService.update(
                postId,
                postUpdateRequest.title(),
                postUpdateRequest.content(),
                postUpdateRequest.categoryId()
                );
        return PostUpdateResponse.from(post);
    }

    //삭제 하는 경우 -> soft delete 처리 된다
    @DeleteMapping("/{postId}")
    public PostDeleteResponse delete(@PathVariable Long postId) {
        postService.delete(postId);
        return new PostDeleteResponse(postId,"삭제되었습니다.");
    }

}
