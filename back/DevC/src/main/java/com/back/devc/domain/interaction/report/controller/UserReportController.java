package com.back.devc.domain.interaction.report.controller;

import com.back.devc.domain.interaction.report.dto.ReportRequestDTO;
import com.back.devc.domain.interaction.report.service.UserReportService;
import com.back.devc.global.response.SuccessCode;
import com.back.devc.global.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class UserReportController {

    private final UserReportService reportService;

    @PostMapping("/post")
    public ResponseEntity<SuccessResponse<Void>> reportPost(
            @RequestBody ReportRequestDTO requestDto,
            @SessionAttribute(name = "memberId") Long reporterId
    ) {
        reportService.reportPost(reporterId, requestDto);

        return ResponseEntity.ok(
                SuccessResponse.of(SuccessCode.REPORT_SUCCESS, null)
        );
    }

    @PostMapping("/comment")
    public ResponseEntity<SuccessResponse<Void>> reportComment(
            @RequestBody ReportRequestDTO requestDto,
            @SessionAttribute(name = "memberId") Long reporterId
    ) {
        reportService.reportComment(reporterId, requestDto);

        return ResponseEntity.ok(
                SuccessResponse.of(SuccessCode.REPORT_SUCCESS, null)
        );
    }
}