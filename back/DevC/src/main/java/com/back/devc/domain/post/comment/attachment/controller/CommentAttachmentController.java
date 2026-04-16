package com.back.devc.domain.post.comment.attachment.controller;

import com.back.devc.domain.post.comment.attachment.dto.CommentAttachmentDeleteResponse;
import com.back.devc.domain.post.comment.attachment.dto.CommentAttachmentListResponse;
import com.back.devc.domain.post.comment.attachment.service.CommentAttachmentService;
import com.back.devc.global.security.jwt.JwtPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/comments/{commentId}/attachments")
@RequiredArgsConstructor
public class CommentAttachmentController {

    private final CommentAttachmentService commentAttachmentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommentAttachmentListResponse> uploadCommentAttachments(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long commentId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "fileOrder", required = false) List<Integer> fileOrders
    ) {
        getAuthenticatedUserId(principal);

        return ResponseEntity.ok(
                commentAttachmentService.uploadAttachments(commentId, files, fileOrders)
        );
    }

    @GetMapping
    public ResponseEntity<CommentAttachmentListResponse> getCommentAttachments(
            @PathVariable Long commentId
    ) {
        return ResponseEntity.ok(
                commentAttachmentService.getAttachments(commentId)
        );
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<CommentAttachmentDeleteResponse> deleteCommentAttachment(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long commentId,
            @PathVariable Long attachmentId
    ) {
        getAuthenticatedUserId(principal);

        return ResponseEntity.ok(
                commentAttachmentService.deleteAttachment(commentId, attachmentId)
        );
    }

    private Long getAuthenticatedUserId(JwtPrincipal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }
        return principal.userId();
    }
}