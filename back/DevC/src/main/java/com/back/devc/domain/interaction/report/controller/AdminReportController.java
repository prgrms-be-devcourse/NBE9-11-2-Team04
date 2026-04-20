package com.back.devc.domain.interaction.report.controller;

import com.back.devc.domain.interaction.report.dto.AdminReportRequestDTO;
import com.back.devc.domain.interaction.report.dto.ReportResponseDTO;
import com.back.devc.domain.interaction.report.service.AdminReportService;
import static com.back.devc.global.security.jwt.JwtPrincipalHelper.getAuthenticatedUserId;

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

        if (principal == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

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
        if (principal == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        adminReportService.approveReport(principal.userId(), requestDto);

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
        if (principal == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        adminReportService.rejectReport(principal.userId(), requestDto);

        return ResponseEntity.ok(SuccessResponse.of("REPORT_REJECT_200", "신고 반려 완료", null));
    }
}