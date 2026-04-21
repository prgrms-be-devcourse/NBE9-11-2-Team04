package com.back.devc.domain.interaction.report.dto;

import com.back.devc.domain.interaction.report.entity.SanctionType;

public record AdminReportRequestDTO(
        Long reportId,
        String targetType,
        String adminNote,
        SanctionType sanctionType,
        Integer suspensionDays
) {
}