package com.back.devc.global.response;

import com.back.devc.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
        String code,
        String message,
        LocalDateTime timestamp,
        Map<String, String> validation
) {
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                LocalDateTime.now(),
                Map.of()
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, Map<String, String> validation) {
        return new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                LocalDateTime.now(),
                validation
        );
    }
}
