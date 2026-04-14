package com.back.devc.domain.interaction.report.controller;

import com.back.devc.domain.interaction.report.dto.AdminReportRequestDTO;
import com.back.devc.domain.interaction.report.dto.ReportResponseDTO;
import com.back.devc.domain.interaction.report.service.AdminReportService;
import com.back.devc.global.exception.ApiException;
import com.back.devc.global.exception.ErrorCode;
import com.back.devc.global.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;

    /**
     * 대기 중인 신고 목록 조회 (PENDING 상태만)
     */
    @GetMapping("/pending")
    public ResponseEntity<SuccessResponse<List<ReportResponseDTO>>> getPendingReports(
            @SessionAttribute(name = "memberRole") String role
    ) {
        validateAdminRole(role);
        List<ReportResponseDTO> reports = adminReportService.getPendingReports();
        return ResponseEntity.ok(SuccessResponse.of("ADMIN_200", "신고 대기 목록 조회 성공", reports));
    }

    /**
     * 신고 승인 및 제재 처리
     */
    @PostMapping("/approve")
    public ResponseEntity<SuccessResponse<Void>> approveReport(
            @RequestBody AdminReportRequestDTO requestDto,
            @SessionAttribute(name = "memberId") Long adminId,
            @SessionAttribute(name = "memberRole") String role
    ) {
        validateAdminRole(role);
        adminReportService.approveReport(adminId, requestDto);
        return ResponseEntity.ok(SuccessResponse.of("REPORT_APPROVE_200", "신고 승인 및 제재 완료", null));
    }

    /**
     * 신고 반려
     */
    @PostMapping("/reject")
    public ResponseEntity<SuccessResponse<Void>> rejectReport(
            @RequestBody AdminReportRequestDTO requestDto,
            @SessionAttribute(name = "memberId") Long adminId,
            @SessionAttribute(name = "memberRole") String role
    ) {
        validateAdminRole(role);
        adminReportService.rejectReport(adminId, requestDto);
        return ResponseEntity.ok(SuccessResponse.of("REPORT_REJECT_200", "신고 반려 완료", null));
    }

    private void validateAdminRole(String role) {
        if (!"ADMIN".equals(role)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
    }
}