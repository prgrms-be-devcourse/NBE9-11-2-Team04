package com.back.devc.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_400", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_401", "인증이 필요합니다."),
    ALREADY_DELETED(HttpStatus.GONE, "COMMON_410", "이미 삭제된 리소스입니다."),

    EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_404_EMAIL_NOT_FOUND", "존재하지 않는 이메일입니다."),
    PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "AUTH_401_PASSWORD_MISMATCH", "비밀번호가 일치하지 않습니다."),
    MEMBER_BLACKLISTED(HttpStatus.FORBIDDEN, "AUTH_403_MEMBER_BLACKLISTED", "이용할 수 없는 계정입니다."),

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_INVALID_TOKEN", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_EXPIRED_TOKEN", "만료된 토큰입니다."),
    INVALID_TOKEN_TYPE(HttpStatus.UNAUTHORIZED, "AUTH_401_INVALID_TOKEN_TYPE", "토큰 타입이 올바르지 않습니다."),

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_404_NOT_FOUND", "회원을 찾을 수 없습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH_409_EMAIL", "이미 사용 중인 이메일입니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH_409_NICKNAME", "이미 사용 중인 닉네임입니다."),

    OAUTH2_PENDING_SIGNUP_REQUIRED(
            HttpStatus.UNAUTHORIZED,
            "AUTH_401_OAUTH2_PENDING_SIGNUP_REQUIRED",
            "OAuth 회원가입 정보가 없습니다. 다시 GitHub 로그인을 진행해주세요."
    ),
    OAUTH2_PENDING_SIGNUP_EXPIRED(
            HttpStatus.UNAUTHORIZED,
            "AUTH_401_OAUTH2_PENDING_SIGNUP_EXPIRED",
            "OAuth 회원가입 세션이 만료되었습니다. 다시 GitHub 로그인을 진행해주세요."
    ),

    REPORT_ALREADY_EXISTS(HttpStatus.CONFLICT, "REPORT_409", "이미 신고 처리된 요청입니다."),
    CANNOT_REPORT_SELF(HttpStatus.BAD_REQUEST, "REPORT_400_SELF", "본인의 게시글이나 댓글은 신고할 수 없습니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_404", "존재하지 않는 게시글입니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_404", "존재하지 않는 댓글입니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT_404_NOT_FOUND", "해당 신고 내역을 찾을 수 없습니다."),
    REPORT_ALREADY_PROCESSED(HttpStatus.CONFLICT, "REPORT_409_ALREADY_PROCESSED", "이미 처리된 신고입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
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
