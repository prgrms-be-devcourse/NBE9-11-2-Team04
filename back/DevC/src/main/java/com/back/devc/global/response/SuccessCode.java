package com.back.devc.global.response;

import org.springframework.http.HttpStatus;

/**
 * 공통 성공 코드 인터페이스
 *
 * - 각 도메인의 SuccessCode enum이 구현한다.
 */
public interface SuccessCode {

    HttpStatus getStatus();

    String getMessage();
}