package com.back.devc.domain.interaction.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportRequestDTO {

    @NotNull(message = "신고 대상 ID는 필수입니다.")
    private Long targetId;      // 게시글 번호 또는 댓글 번호

    @NotBlank(message = "신고 유형은 필수입니다.")
    private String reasonType;  // ABUSE, SPAM, HATE, ETC

    private String reasonDetail; // 상세 내용 (선택)
}