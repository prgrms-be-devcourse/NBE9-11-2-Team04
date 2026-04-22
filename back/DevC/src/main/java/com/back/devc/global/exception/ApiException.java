package com.back.devc.global.exception;

import com.back.devc.global.exception.errorCode.AuthErrorCode;
import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;
    private final AuthErrorCode authErrorCode;

    public ApiException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.authErrorCode = null;
    }

    public ApiException(AuthErrorCode authErrorCode) {
        super(authErrorCode.getMessage());
        this.errorCode = null;
        this.authErrorCode = authErrorCode;
    }

}
