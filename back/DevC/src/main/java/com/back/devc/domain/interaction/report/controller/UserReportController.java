package com.back.devc.domain.interaction.report.controller;

import com.back.devc.domain.interaction.report.dto.ReportRequestDTO;
import com.back.devc.domain.interaction.report.service.UserReportService;
import com.back.devc.global.response.SuccessCode;
import com.back.devc.global.response.SuccessResponse;
import com.back.devc.global.security.jwt.JwtPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class UserReportController {

    private final UserReportService reportService;

    /**
     * 게시글 신고
     */
    @PostMapping("/post")
    public ResponseEntity<SuccessResponse<Void>> reportPost(
            @RequestBody @Valid ReportRequestDTO requestDto,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        reportService.reportPost(getAuthenticatedUserId(principal), requestDto);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.REPORT_SUCCESS, null));
    }

    /**
     * 댓글 신고
     */
    @PostMapping("/comment")
    public ResponseEntity<SuccessResponse<Void>> reportComment(
            @RequestBody @Valid ReportRequestDTO requestDto,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        reportService.reportComment(getAuthenticatedUserId(principal), requestDto);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.REPORT_SUCCESS, null));
    }

    /**
     * 신고 컨트롤러에서 공통으로 사용하는 로그인 사용자 식별 메서드
     *
     * JwtAuthenticationFilter가 정상적으로 principal을 세팅한 경우 userId를 반환하고,
     * 인증 정보가 없으면 NPE 대신 401 UNAUTHORIZED를 반환하도록 방어
     */
    private Long getAuthenticatedUserId(JwtPrincipal principal) {
        // 토큰이 없거나 필터에서 principal을 세팅하지 못한 요청은 인증 실패로 처리한다.
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }
        return principal.userId();
    }
}