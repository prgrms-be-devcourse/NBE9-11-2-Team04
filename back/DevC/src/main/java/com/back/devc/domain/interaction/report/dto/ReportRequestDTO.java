package com.back.devc.domain.interaction.report.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportRequestDTO {
    private Long targetId;     // 게시글 번호 또는 댓글 번호
    private String reasonType;    // ABUSE, SPAM 등
    private String reasonDetail;  // 상세 내용
}