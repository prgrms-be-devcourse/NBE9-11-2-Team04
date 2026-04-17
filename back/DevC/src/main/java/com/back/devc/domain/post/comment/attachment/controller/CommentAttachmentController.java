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

    /**
     * 댓글 첨부파일 업로드
     *
     * 첨부파일 업로드/삭제는 로그인한 사용자만 가능하도록 제한
     * 따라서 현재 로그인 사용자를 SecurityContext 안의 JwtPrincipal 에서 확인한 뒤 처리
     */
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

    /**
     * 댓글 첨부파일 삭제
     *
     * 업로드와 동일하게 현재 로그인한 사용자 기준으로만 요청을 허용
     */
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

    /**
     * 컨트롤러에서 공통으로 사용하는 로그인 사용자 식별 메서드
     *
     * principal 이 없으면 비로그인 요청이므로 401을 반환하고,
     * 있으면 JwtPrincipal 안의 userId를 꺼내 현재 사용자로 사용
     */
    private Long getAuthenticatedUserId(JwtPrincipal principal) {
        // 인증 없이 첨부파일 관련 API를 호출한 경우 명확하게 401 UNAUTHORIZED 로 응답한다.
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }
        return principal.userId();
    }
}