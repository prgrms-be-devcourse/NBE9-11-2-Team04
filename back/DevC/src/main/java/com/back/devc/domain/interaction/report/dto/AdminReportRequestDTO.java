package com.back.devc.domain.interaction.report.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminReportRequestDTO {

    @NotNull(message = "신고 ID는 필수입니다.")
    private Long reportId;

    private String sanctionType;  // 제재 유형 (WARNING, TEMP_BAN, PERM_BAN)

    private String rejectReason;  // 반려 사유 - 반려 시 사용
}