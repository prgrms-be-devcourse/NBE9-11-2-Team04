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
    private final String targetType; // POST, COMMENT
    private final Long targetId;
    private final String reasonType;
    private final String reasonDetail;
    private final String status;
    private final LocalDateTime createdAt;

    // 엔티티를 DTO로 변환하는 정적 메서드
    public static ReportResponseDTO from(Report report) {
        return new ReportResponseDTO(
                report.getReportId(),
                report.getReporter().getUserId(),
                report.getReporter().getNickname(),
                report.getTargetType(),
                report.getTargetId().longValue(), // Integer를 Long으로 변환 (필요시)
                report.getReasonType(),
                report.getReasonDetail(),
                report.getStatus(),
                report.getCreatedAt()
        );
    }
}