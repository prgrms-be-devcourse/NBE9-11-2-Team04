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
     * 현재 로그인한 사용자의 정보를 SecurityContext 안의 JwtPrincipal 에서 꺼내 사용한
     * 이렇게 해야 다른 사용자의 userId를 임의로 넣어 댓글을 작성하는 것을 막음
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
     * 컨트롤러에서 공통으로 사용하는 로그인 사용자 식별 메서드
     *
     * principal 이 없으면 비로그인 요청이므로 401을 반환하고,
     * 있으면 JwtPrincipal 안의 userId를 꺼내 service 계층으로 넘김
     */
    private Long getAuthenticatedUserId(JwtPrincipal principal) {
        // 인증 없이 댓글 관련 API를 호출한 경우 명확하게 401 UNAUTHORIZED 로 응답한다.
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }
        return principal.userId();
    }
}