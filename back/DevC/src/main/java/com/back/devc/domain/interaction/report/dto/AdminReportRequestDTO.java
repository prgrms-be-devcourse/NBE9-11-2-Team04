package com.back.devc.domain.interaction.report.dto;

import com.back.devc.domain.interaction.report.entity.SanctionType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminReportRequestDTO {

    /**
     * 그룹 처리 시 targetId 값을 이 필드로 전달한다.
     * (단건 처리 시에는 실제 reportId)
     */
    private Long reportId;

    /**
     * 그룹 처리 시 필수. "POST" | "COMMENT"
     */
    private String targetType;

    /**
     * 관리자 메모 (선택)
     */
    private String adminNote;

    /**
     * 제재 유형: WARNED | SUSPENDED | BLACKLISTED
     * 반려(reject) 시에는 null 허용
     */
    private SanctionType sanctionType;

    /**
     * SUSPENDED 선택 시 정지 기간(일). 1 | 3 | 7 | 30
     * sanctionType이 SUSPENDED가 아니면 무시된다.
     */
    private Integer suspensionDays;
}