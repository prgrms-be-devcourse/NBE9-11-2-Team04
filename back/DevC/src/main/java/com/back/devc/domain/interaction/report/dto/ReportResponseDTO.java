package com.back.devc.domain.interaction.report.dto;

import com.back.devc.domain.interaction.report.entity.Report;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReportResponseDTO {
    private final Long reportId;
    private final Long reporterId;
    private final String reporterNickname;
    private final String targetType;        // POST, COMMENT
    private final Long targetId;
    private final String reasonType;
    private final String reasonDetail;
    private final String status;            // PENDING, RESOLVED, REJECTED
    private final LocalDateTime createdAt;
    private final LocalDateTime processedAt;

    public static ReportResponseDTO from(Report report) {
        return new ReportResponseDTO(
                report.getReportId(),
                report.getReporter().getUserId(),
                report.getReporter().getNickname(),
                report.getTargetType(),
                report.getTargetId(),
                report.getReasonType(),
                report.getReasonDetail(),
                report.getStatus(),
                report.getCreatedAt(),
                report.getProcessedAt()
        );
    }
}