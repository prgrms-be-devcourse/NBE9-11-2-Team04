package com.back.devc.global.response;

import org.springframework.http.HttpStatus;

public enum SuccessCode {
    LOGIN_SUCCESS(HttpStatus.OK, "AUTH_200_LOGIN_SUCCESS", "로그인에 성공했습니다."),
    LOGOUT_SUCCESS(HttpStatus.OK, "AUTH_200_LOGOUT_SUCCESS", "로그아웃이 완료되었습니다."),
    SIGN_UP_SUCCESS(HttpStatus.CREATED, "AUTH_201_SIGNUP_SUCCESS", "회원가입이 완료되었습니다."),

    MEMBER_LIST_SUCCESS(HttpStatus.OK, "ADM_200_MEMBER_LIST", "회원 목록 조회가 완료되었습니다."),
    MEMBER_DETAIL_SUCCESS(HttpStatus.OK, "ADM_200_MEMBER_DETAIL", "회원 상세 정보 조회가 완료되었습니다."),
    MEMBER_STATUS_UPDATE_SUCCESS(HttpStatus.OK, "ADM_200_MEMBER_STATUS_UPDATE", "회원 상태 변경이 완료되었습니다."),
    MEMBER_SEARCH_SUCCESS(HttpStatus.OK, "ADM_200_MEMBER_SEARCH", "회원 검색이 완료되었습니다."),

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
    COMMENT_LIST_SUCCESS(HttpStatus.OK, "COMMENT_200_LIST", "댓글 목록 조회 성공");

    private final HttpStatus status;
    private final String code;
    private final String message;

    SuccessCode(HttpStatus status, String code, String message) {
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
