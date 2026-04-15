package com.back.devc.global.response;

import org.springframework.http.HttpStatus;

public enum SuccessCode {
    LOGIN_SUCCESS(HttpStatus.OK, "AUTH_200_LOGIN_SUCCESS", "로그인에 성공했습니다."),
    LOGOUT_SUCCESS(HttpStatus.OK, "AUTH_200_LOGOUT_SUCCESS", "로그아웃이 완료되었습니다."),
    REISSUE_SUCCESS(HttpStatus.OK, "AUTH_200_REISSUE_SUCCESS", "토큰 재발급에 성공했습니다."),
    ME_SUCCESS(HttpStatus.OK, "USER_200_ME_SUCCESS", "내 정보 조회에 성공했습니다."),
    SIGN_UP_SUCCESS(HttpStatus.CREATED, "AUTH_201_SIGNUP_SUCCESS", "회원가입이 완료되었습니다."),

    // 관리자 유저 관리
    MEMBER_LIST_SUCCESS(HttpStatus.OK, "ADM_200_MEMBER_LIST", "회원 목록 조회가 완료되었습니다."),
    MEMBER_DETAIL_SUCCESS(HttpStatus.OK, "ADM_200_MEMBER_DETAIL", "회원 상세 정보 조회가 완료되었습니다."),
    MEMBER_STATUS_UPDATE_SUCCESS(HttpStatus.OK, "ADM_200_MEMBER_STATUS_UPDATE", "회원 상태 변경이 완료되었습니다."),
    MEMBER_SEARCH_SUCCESS(HttpStatus.OK, "ADM_200_MEMBER_SEARCH", "회원 검색이 완료되었습니다.");

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
