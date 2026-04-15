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
        // JWT principal.userId() 사용
        reportService.reportPost(principal.userId(), requestDto);
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
        reportService.reportComment(principal.userId(), requestDto);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.REPORT_SUCCESS, null));
    }
}