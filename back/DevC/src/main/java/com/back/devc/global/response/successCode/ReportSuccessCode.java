package com.back.devc.global.response.successCode;

import org.springframework.http.HttpStatus;

public enum ReportSuccessCode {

    // 신고 관련 성공 코드
    REPORT_LIST_SUCCESS(HttpStatus.OK, "REPORT_200_LIST", "신고 목록 조회 성공"),
    REPORT_GROUP_LIST_SUCCESS(HttpStatus.OK, "REPORT_200_GROUP_LIST", "그룹 신고 조회 성공"),
    REPORT_GROUP_APPROVE_SUCCESS(HttpStatus.OK, "REPORT_200_GROUP_APPROVE", "그룹 신고 승인 완료"),
    REPORT_GROUP_REJECT_SUCCESS(HttpStatus.OK, "REPORT_200_GROUP_REJECT", "그룹 신고 반려 완료"),

    REPORT_POST_SUCCESS(HttpStatus.CREATED, "REPORT_201_POST", "게시글 신고가 정상적으로 접수되었습니다."),
    REPORT_COMMENT_SUCCESS(HttpStatus.CREATED, "REPORT_201_COMMENT", "댓글 신고가 정상적으로 접수되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ReportSuccessCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
