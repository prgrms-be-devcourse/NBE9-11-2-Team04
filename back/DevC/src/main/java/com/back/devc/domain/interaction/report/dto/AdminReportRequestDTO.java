package com.back.devc.domain.interaction.report.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminReportRequestDTO {
    private Long reportId;
    private String sanctionType;  // 제재 유형 (예: WARNING, TEMP_BAN, PERM_BAN)
    private String rejectReason;  // 반려 시 사유
}