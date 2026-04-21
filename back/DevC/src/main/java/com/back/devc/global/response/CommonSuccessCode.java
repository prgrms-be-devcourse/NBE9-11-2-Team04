package com.back.devc.global.response;

import org.springframework.http.HttpStatus;

public enum CommonSuccessCode implements SuccessCode {

    REPORT_SUCCESS(HttpStatus.OK, "REPORT_200", "신고가 정상적으로 접수되었습니다."),

    NOTIFICATION_LIST_SUCCESS(HttpStatus.OK, "NOTIFICATION_200_LIST", "알림 목록 조회 성공"),
    NOTIFICATION_READ_SUCCESS(HttpStatus.OK, "NOTIFICATION_200_READ", "알림 읽음 처리 성공"),

    COMMENT_ATTACHMENT_UPLOAD_SUCCESS(HttpStatus.OK, "COMMENT_ATTACHMENT_200_UPLOAD", "댓글 첨부파일 업로드 성공"),
    COMMENT_ATTACHMENT_LIST_SUCCESS(HttpStatus.OK, "COMMENT_ATTACHMENT_200_LIST", "댓글 첨부파일 조회 성공"),
    COMMENT_ATTACHMENT_DELETE_SUCCESS(HttpStatus.OK, "COMMENT_ATTACHMENT_200_DELETE", "댓글 첨부파일 삭제 성공"),

    COMMENT_CREATE_SUCCESS(HttpStatus.OK, "COMMENT_200_CREATE", "댓글 작성 성공"),
    COMMENT_REPLY_SUCCESS(HttpStatus.OK, "COMMENT_200_REPLY", "대댓글 작성 성공"),
    COMMENT_UPDATE_SUCCESS(HttpStatus.OK, "COMMENT_200_UPDATE", "댓글 수정 성공"),
    COMMENT_DELETE_SUCCESS(HttpStatus.OK, "COMMENT_200_DELETE", "댓글 삭제 성공"),
    COMMENT_LIST_SUCCESS(HttpStatus.OK, "COMMENT_200_LIST", "댓글 목록 조회 성공"),

    WITHDRAW_SUCCESS(HttpStatus.OK, "USER_200_WITHDRAW_SUCCESS", "회원 탈퇴가 완료되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    CommonSuccessCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}