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

    /**
     * 댓글 작성
     *
     * 댓글 작성자는 프론트에서 직접 넘기지 않고,
     * 현재 로그인한 사용자의 정보를 SecurityContext 안의 JwtPrincipal에서 꺼내 사용한다.
     * 이렇게 해야 다른 사용자의 userId를 임의로 넣어 댓글을 작성하는 것을 막을 수 있다.
     */
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long postId,
            @RequestBody @Valid CommentCreateRequest request
    ) {
        return ResponseEntity.ok(commentService.createComment(postId, getAuthenticatedUserId(principal), request));
    }

    /**
     * 대댓글(답글) 작성
     *
     * 답글도 일반 댓글과 동일하게 현재 로그인한 사용자 기준으로만 작성
     */
    @PostMapping("/comments/{commentId}/replies")
    public ResponseEntity<CommentResponse> createReply(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long commentId,
            @RequestBody @Valid CommentCreateRequest request
    ) {
        return ResponseEntity.ok(commentService.createReply(commentId, getAuthenticatedUserId(principal), request));
    }

    /**
     * 댓글 수정
     *
     * 수정 요청 역시 현재 로그인한 사용자 기준으로 처리해서,
     * service 계층에서 "본인이 작성한 댓글인지"를 검증할 수 있게 함
     */
    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long commentId,
            @RequestBody @Valid CommentUpdateRequest request
    ) {
        return ResponseEntity.ok(commentService.updateComment(commentId, getAuthenticatedUserId(principal), request));
    }

    /**
     * 댓글 삭제
     *
     * 삭제도 현재 로그인한 사용자 기준으로만 처리
     * 실제 삭제 가능 여부(본인 댓글인지 등)는 service 계층에서 검증
     */
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

    /**
     * 댓글 컨트롤러에서 공통으로 사용하는 로그인 사용자 식별 메서드.
     *
     * JwtAuthenticationFilter가 정상적으로 principal을 세팅한 경우 userId를 반환하고,
     * 인증 정보가 없으면 비로그인 요청으로 간주해 401 UNAUTHORIZED를 반환한다.
     */
    private Long getAuthenticatedUserId(JwtPrincipal principal) {
        // 토큰이 없거나 필터에서 principal을 세팅하지 못한 요청은 인증 실패로 처리한다.
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }
        return principal.userId();
    }
}