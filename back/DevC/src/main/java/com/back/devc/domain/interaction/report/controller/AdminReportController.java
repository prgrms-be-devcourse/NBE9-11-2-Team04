package com.back.devc.domain.interaction.report.controller;

import com.back.devc.domain.interaction.report.dto.AdminReportRequestDTO;
import com.back.devc.domain.interaction.report.dto.ReportGroupResponseDTO;
import com.back.devc.domain.interaction.report.dto.ReportResponseDTO;
import com.back.devc.domain.interaction.report.entity.ReportStatus;
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

    /* =========================
       RAW REPORT (개별 로그)
    ========================= */
    @GetMapping("/raw")
    public ResponseEntity<SuccessResponse<Page<ReportResponseDTO>>> getReports(
            @RequestParam(required = false) ReportStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<ReportResponseDTO> reports = adminReportService.getReports(status, pageable);
        return ResponseEntity.ok(
                SuccessResponse.of("ADMIN_200", "신고 목록 조회 성공", reports)
        );
    }

    /* =========================
       GROUPED REPORT (핵심)
    ========================= */
    @GetMapping("/groups")
    public ResponseEntity<SuccessResponse<Page<ReportGroupResponseDTO>>> getGrouped(
            @RequestParam(required = false) ReportStatus status,
            @PageableDefault(size = 20, sort = "latestCreatedAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<ReportGroupResponseDTO> result = adminReportService.getGroupedReports(status, pageable);
        return ResponseEntity.ok(
                SuccessResponse.of("ADMIN_200", "그룹 신고 조회 성공", result)
        );
    }

    /* =========================
       GROUP APPROVE
       Body: { reportId(=targetId), targetType, sanctionType, suspensionDays, adminNote }
    ========================= */
    @PostMapping("/groups/approve")
    public ResponseEntity<SuccessResponse<Void>> approveGroup(
            @RequestBody AdminReportRequestDTO requestDto,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        adminReportService.approveReportGroup(getAuthenticatedUserId(principal), requestDto);
        return ResponseEntity.ok(
                SuccessResponse.of("REPORT_APPROVE_200", "그룹 신고 승인 완료", null)
        );
    }

    /* =========================
       GROUP REJECT
       Body: { reportId(=targetId), targetType, adminNote }
    ========================= */
    @PostMapping("/groups/reject")
    public ResponseEntity<SuccessResponse<Void>> rejectGroup(
            @RequestBody AdminReportRequestDTO requestDto,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        adminReportService.rejectReportGroup(getAuthenticatedUserId(principal), requestDto);
        return ResponseEntity.ok(
                SuccessResponse.of("REPORT_REJECT_200", "그룹 신고 반려 완료", null)
        );
    }

    private Long getAuthenticatedUserId(JwtPrincipal principal) {
        if (principal == null) throw new ApiException(ErrorCode.UNAUTHORIZED);
        return principal.userId();
    }
}