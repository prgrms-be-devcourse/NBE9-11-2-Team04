package com.back.devc.domain.interaction.report.dto;

import com.back.devc.domain.interaction.report.entity.Report;
import com.back.devc.domain.interaction.report.entity.ReportStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReportResponseDTO {

    private final Long reportId;

    private final String reporterEmail;
    private final String reporterNickname;

    private final String targetType;
    private final Long targetId;

    private final String targetNickname;
    private final String targetTitle;
    private final String targetContent;

    private final String reasonType;
    private final String reasonDetail;

    private final ReportStatus status;

    private final LocalDateTime createdAt;
    private final LocalDateTime processedAt;

    public static ReportResponseDTO of(
            Report report,
            String targetNickname,
            String targetTitle,
            String targetContent
    ) {
        return new ReportResponseDTO(
                report.getReportId(),
                report.getReporter().getEmail(),
                report.getReporter().getNickname(),
                report.getTargetType(),
                report.getTargetId(),
                targetNickname,
                targetTitle,
                targetContent,
                report.getReasonType(),
                report.getReasonDetail(),
                report.getStatus(),
                report.getCreatedAt(),
                report.getProcessedAt()
        );
    }
}