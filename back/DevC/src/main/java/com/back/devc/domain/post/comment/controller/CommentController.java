package com.back.devc.domain.post.comment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.back.devc.domain.post.comment.dto.CommentCreateRequest;
import com.back.devc.domain.post.comment.dto.CommentDeleteResponse;
import com.back.devc.domain.post.comment.dto.CommentListResponse;
import com.back.devc.domain.post.comment.dto.CommentResponse;
import com.back.devc.domain.post.comment.dto.CommentUpdateRequest;
import com.back.devc.domain.post.comment.service.CommentService;
import com.back.devc.global.security.jwt.JwtPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long postId,
            @RequestBody @Valid CommentCreateRequest request
    ) {
        return ResponseEntity.ok(commentService.createComment(postId, getAuthenticatedUserId(principal), request));
    }

    @PostMapping("/comments/{commentId}/replies")
    public ResponseEntity<CommentResponse> createReply(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long commentId,
            @RequestBody @Valid CommentCreateRequest request
    ) {
        return ResponseEntity.ok(commentService.createReply(commentId, getAuthenticatedUserId(principal), request));
    }

    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long commentId,
            @RequestBody @Valid CommentUpdateRequest request
    ) {
        return ResponseEntity.ok(commentService.updateComment(commentId, getAuthenticatedUserId(principal), request));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<CommentDeleteResponse> deleteComment(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long commentId
    ) {
        return ResponseEntity.ok(commentService.deleteComment(commentId, getAuthenticatedUserId(principal)));
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentListResponse> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getComments(postId));
    }

    private Long getAuthenticatedUserId(JwtPrincipal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }
        return principal.userId();
    }
}