package com.back.devc.global.response.successCode;

import com.back.devc.global.response.SuccessCodeSpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReportSuccessCode implements SuccessCodeSpec {

    // 신고 관련 성공 코드
    REPORT_200_LIST(HttpStatus.OK, "REPORT_200_LIST", "신고 목록 조회 성공"),
    REPORT_200_GROUP_LIST(HttpStatus.OK, "REPORT_200_GROUP_LIST", "그룹 신고 조회 성공"),
    REPORT_200_GROUP_APPROVE(HttpStatus.OK, "REPORT_200_GROUP_APPROVE", "그룹 신고 승인 완료"),
    REPORT_200_GROUP_REJECT(HttpStatus.OK, "REPORT_200_GROUP_REJECT", "그룹 신고 반려 완료"),

    REPORT_201_POST(HttpStatus.CREATED, "REPORT_201_POST", "게시글 신고가 정상적으로 접수되었습니다."),
    REPORT_201_COMMENT(HttpStatus.CREATED, "REPORT_201_COMMENT", "댓글 신고가 정상적으로 접수되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
