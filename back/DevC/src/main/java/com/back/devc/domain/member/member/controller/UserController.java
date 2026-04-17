package com.back.devc.domain.member.member.controller;

import com.back.devc.domain.member.member.dto.MyInfoResponse;
import com.back.devc.domain.member.member.service.MemberService;
import com.back.devc.global.exception.ApiException;
import com.back.devc.global.exception.ErrorCode;
import com.back.devc.global.response.SuccessCode;
import com.back.devc.global.response.SuccessResponse;
import com.back.devc.global.security.jwt.JwtPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final MemberService memberService;

    // 사용자 정보 조회 API
    @GetMapping("/me")
    public ResponseEntity<SuccessResponse<MyInfoResponse>> me(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        if (principal == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        MyInfoResponse response = memberService.getMyInfo(principal.userId());
        SuccessCode successCode = SuccessCode.ME_SUCCESS;
        return ResponseEntity
                .status(successCode.getStatus())
                .body(SuccessResponse.of(successCode, response));
    }

    // 회원 탈퇴 API
    @DeleteMapping("/me")
    public ResponseEntity<SuccessResponse<String>> withdraw(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        if (principal == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        // 회원 탈퇴 처리
        memberService.withdraw(principal.userId());

        // 세션 종료
        SecurityContextHolder.clearContext();

        // JWT 토큰 만료 쿠키 삭제
        ResponseCookie cookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)  // 쿠키 만료
                .sameSite("Strict")
                .build();

        // 탈퇴 후 응답 반환
        SuccessCode successCode = SuccessCode.WITHDRAW_SUCCESS;
        return ResponseEntity
                .status(successCode.getStatus())
                .header(HttpHeaders.SET_COOKIE, cookie.toString())  // 쿠키 삭제 설정
                .body(SuccessResponse.of(successCode, "회원 탈퇴가 완료되었습니다."));
    }
}