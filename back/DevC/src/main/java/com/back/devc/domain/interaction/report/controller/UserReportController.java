package com.back.devc.domain.interaction.report.controller;

import com.back.devc.domain.interaction.report.dto.ReportRequestDTO;
import com.back.devc.domain.interaction.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class UserReportController {

    private final ReportService reportService;

    /**
     * 게시글 신고
     */
    @PostMapping("/post")
    public ResponseEntity<Map<String, Object>> reportPost(
            @RequestBody ReportRequestDTO requestDto,
            @SessionAttribute(name = "memberId") Long reporterId
    ) {
        // 서비스 호출 (본인 확인, 중복 체크, 저장 로직은 모두 서비스 안에 있음)
        reportService.reportPost(reporterId, requestDto);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "게시글 신고가 접수되었습니다."
        ));
    }

    /**
     * 댓글 신고
     */
    @PostMapping("/comment")
    public ResponseEntity<Map<String, Object>> reportComment(
            @RequestBody ReportRequestDTO requestDto,
            @SessionAttribute(name = "memberId") Long reporterId
    ) {
        reportService.reportComment(reporterId, requestDto);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "댓글 신고가 접수되었습니다."
        ));
    }
}