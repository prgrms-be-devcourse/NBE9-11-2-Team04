package com.back.devc.global.response.successCode;

import org.springframework.http.HttpStatus;

public enum AuthSuccessCode {
    AUTH_200_LOGIN_SUCCESS(HttpStatus.OK, "AUTH_200_LOGIN_SUCCESS", "로그인에 성공했습니다."),
    AUTH_200_LOGOUT_SUCCESS(HttpStatus.OK, "AUTH_200_LOGOUT_SUCCESS", "로그아웃이 완료되었습니다."),
    AUTH_201_SIGNUP_SUCCESS(HttpStatus.CREATED, "AUTH_201_SIGNUP_SUCCESS", "회원가입이 완료되었습니다."),

    OAUTH_200_ME_SUCCESS(HttpStatus.OK, "AUTH_200_OAUTH2_ME_SUCCESS", "OAuth2 사용자 정보 조회에 성공했습니다."),
    OAUTH_200_EXCHANGE_SUCCESS(HttpStatus.OK, "AUTH_200_OAUTH2_EXCHANGE_SUCCESS", "OAuth2 로그인 코드 교환에 성공했습니다."),
    OAUTH_201_SIGNUP_COMPLETE_SUCCESS(HttpStatus.CREATED, "AUTH_201_OAUTH2_SIGNUP_COMPLETE_SUCCESS", "OAuth2 회원가입 완료에 성공했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    AuthSuccessCode(HttpStatus status, String code, String message) {
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
