package com.back.devc.global.response;

import org.springframework.http.HttpStatus;

public enum SuccessCode {
    SIGN_UP_SUCCESS(HttpStatus.CREATED, "AUTH_201_SIGNUP_SUCCESS", "회원가입이 되었습니다.");

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
