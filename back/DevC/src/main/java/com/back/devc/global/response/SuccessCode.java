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

    // 신고 관련 성공 코드
    REPORT_LIST_SUCCESS(HttpStatus.OK, "REPORT_200_LIST", "신고 목록 조회 성공"),
    REPORT_GROUP_LIST_SUCCESS(HttpStatus.OK, "REPORT_200_GROUP_LIST", "그룹 신고 조회 성공"),
    REPORT_GROUP_APPROVE_SUCCESS(HttpStatus.OK, "REPORT_200_GROUP_APPROVE", "그룹 신고 승인 완료"),
    REPORT_GROUP_REJECT_SUCCESS(HttpStatus.OK, "REPORT_200_GROUP_REJECT", "그룹 신고 반려 완료"),

    REPORT_POST_SUCCESS(HttpStatus.CREATED, "REPORT_201_POST", "게시글 신고가 정상적으로 접수되었습니다."),
    REPORT_COMMENT_SUCCESS(HttpStatus.CREATED, "REPORT_201_COMMENT", "댓글 신고가 정상적으로 접수되었습니다."),

    // 대시보드 관련 성공 코드
    DASHBOARD_LIST(HttpStatus.OK, "DASHBOARD_200_LIST", "대시보드 조회 성공"),

    // Notification SuccessCode
    NOTIFICATION_200_LIST_SUCCESS(HttpStatus.OK, "NOTIFICATION_200_LIST_SUCCESS", "알림 목록 조회 성공"),
    NOTIFICATION_200_READ_SUCCESS(HttpStatus.OK, "NOTIFICATION_200_READ_SUCCESS", "알림 읽음 처리 성공"),

    //게시글 관련 성공 코드

    POST_201_CREATE_SUCCESS(HttpStatus.CREATED,"POST_201_CREATE_SUCCESS", "게시글 작성 성공"),
    POST_200_LIST_SUCCESS(HttpStatus.OK,"POST_200_LIST_SUCCESS", "게시글 목록 조회 성공"),
    POST_200_DETAIL_SUCCESS(HttpStatus.OK,"POST_200_DETAIL_SUCCESS", "게시글 상세 조회 성공"),
    POST_200_UPDATE_SUCCESS(HttpStatus.OK,"POST_200_UPDATE_SUCCESS", "게시글 수정 성공"),
    POST_200_DELETE_SUCCESS(HttpStatus.OK,"POST_200_DELETE_SUCCESS", "게시글 삭제 성공"),



    // CommentAttachment SuccessCode
    COMMENT_ATTACHMENT_201_UPLOAD_SUCCESS(HttpStatus.CREATED, "COMMENT_ATTACHMENT_201_UPLOAD_SUCCESS", "댓글 첨부파일 업로드 성공"),
    COMMENT_ATTACHMENT_200_LIST_SUCCESS(HttpStatus.OK, "COMMENT_ATTACHMENT_200_LIST_SUCCESS", "댓글 첨부파일 조회 성공"),
    COMMENT_ATTACHMENT_200_DELETE_SUCCESS(HttpStatus.OK, "COMMENT_ATTACHMENT_200_DELETE_SUCCESS", "댓글 첨부파일 삭제 성공"),

    // Comment SuccessCode
    COMMENT_201_CREATE_SUCCESS(HttpStatus.CREATED, "COMMENT_201_CREATE_SUCCESS", "댓글 작성 성공"),
    COMMENT_201_REPLY_SUCCESS(HttpStatus.CREATED, "COMMENT_201_REPLY_SUCCESS", "대댓글 작성 성공"),
    COMMENT_200_UPDATE_SUCCESS(HttpStatus.OK, "COMMENT_200_UPDATE_SUCCESS", "댓글 수정 성공"),
    COMMENT_200_DELETE_SUCCESS(HttpStatus.OK, "COMMENT_200_DELETE_SUCCESS", "댓글 삭제 성공"),
    COMMENT_200_LIST_SUCCESS(HttpStatus.OK, "COMMENT_200_LIST_SUCCESS", "댓글 목록 조회 성공"),

    // 회원 탈퇴 성공 코드
    WITHDRAW_SUCCESS(HttpStatus.OK, "USER_200_WITHDRAW_SUCCESS", "회원 탈퇴가 완료되었습니다.");

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
