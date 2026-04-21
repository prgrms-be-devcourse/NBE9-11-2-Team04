package com.back.devc.domain.interaction.report.dto;

import com.back.devc.domain.interaction.report.entity.ReportStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ReportGroupResponseDTO(
        String targetType,
        Long targetId,
        String targetNickname,
        String targetTitle,
        String targetContent,
        Long reportCount,
        List<String> reasonTypes,
        ReportStatus status,
        LocalDateTime latestCreatedAt
) {
}