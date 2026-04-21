package com.back.devc.global.response.successCode;

import org.springframework.http.HttpStatus;

public enum MemberSuccessCode {
    MEMBER_ME_SUCCESS(HttpStatus.OK, "MEMBER_200_ME_SUCCESS", "내 정보 조회에 성공했습니다."),
    MEMBER_PUBLIC_PROFILE_GET_SUCCESS(HttpStatus.OK, "MEMBER_200_PUBLIC_PROFILE_GET_SUCCESS", "공개 프로필 조회에 성공했습니다."),
    MEMBER_WITHDRAW_SUCCESS(HttpStatus.OK, "MEMBER_200_WITHDRAW_SUCCESS", "회원 탈퇴가 완료되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    MemberSuccessCode(HttpStatus status, String code, String message) {
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