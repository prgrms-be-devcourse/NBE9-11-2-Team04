package com.back.devc.domain.interaction.report.dto;

import com.back.devc.domain.interaction.report.entity.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ReportGroupResponseDTO {

    private final String targetType;
    private final Long targetId;

    private final String targetNickname;
    private final String targetTitle;
    private final String targetContent;

    private final Long reportCount;

    private final List<String> reasonTypes;

    private final ReportStatus status;

    private final LocalDateTime latestCreatedAt;
}