package com.back.devc.global.response;

import com.back.devc.global.response.successCode.MemberSuccessCode;

import java.time.LocalDateTime;

public record SuccessResponse<T>(
        String code,
        String message,
        LocalDateTime timestamp,
        T data
) {
    public static <T> SuccessResponse<T> of(SuccessCode successCode, T data) {
        return new SuccessResponse<>(
                successCode.getCode(),
                successCode.getMessage(),
                LocalDateTime.now(),
                data
        );
    }

    public static <T> SuccessResponse<T> of(MemberSuccessCode successCode, T data) {
        return new SuccessResponse<>(
                successCode.getCode(),
                successCode.getMessage(),
                LocalDateTime.now(),
                data
        );
    }

    public static <T> SuccessResponse<T> of(String code, String message, T data) {
        return new SuccessResponse<>(code, message, LocalDateTime.now(), data);
    }
}
