package com.back.devc.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 비즈니스 예외 처리 (예: 이메일/닉네임 중복)
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode));
    }

    // 요청값 검증 실패 예외 처리 (@Valid, @NotBlank, @Email 등)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> validation = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() == null ? "잘못된 값입니다." : fieldError.getDefaultMessage(),
                        (existing, replacement) -> existing
                ));

        return ResponseEntity
                .status(ErrorCode.BAD_REQUEST.getStatus())
                .body(ErrorResponse.of(ErrorCode.BAD_REQUEST, validation));
    }
}
