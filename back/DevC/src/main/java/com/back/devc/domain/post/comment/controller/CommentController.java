package com.back.devc.domain.post.comment.controller;

import com.back.devc.domain.post.comment.dto.CommentCreateRequest;
import com.back.devc.domain.post.comment.dto.CommentDeleteResponse;
import com.back.devc.domain.post.comment.dto.CommentListResponse;
import com.back.devc.domain.post.comment.dto.CommentResponse;
import com.back.devc.domain.post.comment.dto.CommentUpdateRequest;
import com.back.devc.domain.post.comment.service.CommentService;
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
            @PathVariable Long postId,
            @RequestBody @Valid CommentCreateRequest request
    ) {
        Long loginUserId = 2L;
        return ResponseEntity.ok(commentService.createComment(postId, loginUserId, request));
    }

    @PostMapping("/comments/{commentId}/replies")
    public ResponseEntity<CommentResponse> createReply(
            @PathVariable Long commentId,
            @RequestBody @Valid CommentCreateRequest request
    ) {
        Long loginUserId = 2L;
        return ResponseEntity.ok(commentService.createReply(commentId, loginUserId, request));
    }

    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @RequestBody @Valid CommentUpdateRequest request
    ) {
        Long loginUserId = 2L;
        return ResponseEntity.ok(commentService.updateComment(commentId, loginUserId, request));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<CommentDeleteResponse> deleteComment(
            @PathVariable Long commentId
    ) {
        Long loginUserId = 1L;
        return ResponseEntity.ok(commentService.deleteComment(commentId, loginUserId));
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentListResponse> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getComments(postId));
    }
}