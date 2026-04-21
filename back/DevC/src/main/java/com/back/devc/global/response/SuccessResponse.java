package com.back.devc.global.response;

import java.time.LocalDateTime;

/**
 * 공통 성공 응답 객체
 *
 * - 모든 API 성공 응답은 이 형식으로 통일한다.
 * - code는 SuccessCode(enum)의 이름을 사용한다.
 */
public record SuccessResponse<T>(
        String code,
        String message,
        LocalDateTime timestamp,
        T data
) {

    /**
     * SuccessCode 기반으로 성공 응답 생성
     *
     * - enum 이름(name)을 code로 사용하여 일관성 유지
     * - timestamp는 서버 생성 시각 기준
     */
    public static <T> SuccessResponse<T> of(SuccessCode successCode, T data) {
        return new SuccessResponse<>(
                successCode.toString(), // enum 이름 사용 (ex: BOOKMARK_201_CREATE)
                successCode.getMessage(),
                LocalDateTime.now(),
                data
        );
    }
}