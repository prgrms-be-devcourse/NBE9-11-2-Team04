package com.back.devc.domain.interaction.report.controller;

import com.back.devc.domain.interaction.report.dto.AdminReportRequestDTO;
import com.back.devc.domain.interaction.report.dto.ReportResponseDTO;
import com.back.devc.domain.interaction.report.service.AdminReportService;
import com.back.devc.global.exception.ApiException;
import com.back.devc.global.exception.ErrorCode;
import com.back.devc.global.response.SuccessResponse;
import com.back.devc.global.security.jwt.JwtPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    private final AdminReportService adminReportService;

    /**
     * 대기 중인 신고 목록 조회 (PENDING 상태만)
     */
    @GetMapping("/pending")
    public ResponseEntity<SuccessResponse<Page<ReportResponseDTO>>> getPendingReports(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {

        getAuthenticatedUserId(principal);

        Page<ReportResponseDTO> reports =
                adminReportService.getPendingReports(pageable);

        return ResponseEntity.ok(
                SuccessResponse.of("ADMIN_200", "신고 대기 목록 조회 성공", reports)
        );
    }

    /**
     * 신고 승인 및 제재 처리
     */
    @PostMapping("/approve")
    public ResponseEntity<SuccessResponse<Void>> approveReport(
            @RequestBody AdminReportRequestDTO requestDto,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        adminReportService.approveReport(getAuthenticatedUserId(principal), requestDto);

        return ResponseEntity.ok(SuccessResponse.of("REPORT_APPROVE_200", "신고 승인 및 제재 완료", null));
    }

    /**
     * 신고 반려
     */
    @PostMapping("/reject")
    public ResponseEntity<SuccessResponse<Void>> rejectReport(
            @RequestBody AdminReportRequestDTO requestDto,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        adminReportService.rejectReport(getAuthenticatedUserId(principal), requestDto);

        return ResponseEntity.ok(SuccessResponse.of("REPORT_REJECT_200", "신고 반려 완료", null));
    }
    /**
     * 관리자 신고 컨트롤러에서 공통으로 사용하는 로그인 사용자 식별 메서드.
     *
     * JwtAuthenticationFilter가 정상적으로 principal을 세팅한 경우 userId를 반환하고,
     * 인증 정보가 없으면 관리자 요청이라도 인증 실패로 간주해 UNAUTHORIZED 예외를 반환한다.
     */
    private Long getAuthenticatedUserId(JwtPrincipal principal) {
        // 토큰이 없거나 필터에서 principal을 세팅하지 못한 요청은 인증 실패로 처리한다.
        if (principal == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        return principal.userId();
    }
}