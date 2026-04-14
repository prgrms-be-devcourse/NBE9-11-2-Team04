package com.back.devc.domain.post.comment.attachment.controller;

import com.back.devc.domain.post.comment.attachment.dto.CommentAttachmentDeleteResponse;
import com.back.devc.domain.post.comment.attachment.dto.CommentAttachmentListResponse;
import com.back.devc.domain.post.comment.attachment.service.CommentAttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/comments/{commentId}/attachments")
@RequiredArgsConstructor
public class CommentAttachmentController {

    private final CommentAttachmentService commentAttachmentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommentAttachmentListResponse> uploadCommentAttachments(
            @PathVariable Long commentId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "fileOrder", required = false) List<Integer> fileOrders
    ) {
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
            @PathVariable Long commentId,
            @PathVariable Long attachmentId
    ) {
        return ResponseEntity.ok(
                commentAttachmentService.deleteAttachment(commentId, attachmentId)
        );
    }
}