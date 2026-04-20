package com.back.devc.domain.member.member.controller;

import com.back.devc.domain.member.member.dto.MyInfoResponse;
import com.back.devc.domain.member.member.dto.PublicProfileResponse;
import com.back.devc.domain.member.member.service.MemberService;
import com.back.devc.global.exception.ApiException;
import com.back.devc.global.exception.ErrorCode;
import com.back.devc.global.response.SuccessCode;
import com.back.devc.global.response.SuccessResponse;
import com.back.devc.global.security.jwt.JwtPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class MemberController {

    private final MemberService memberService;

    @Value("${custom.jwt.access-cookie-name:access_token}")
    private String accessCookieName;

    @Value("${custom.jwt.access-cookie-secure:false}")
    private boolean accessCookieSecure;

    @Value("${custom.jwt.access-cookie-same-site:Lax}")
    private String accessCookieSameSite;

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

    @GetMapping("/{userId}/profile")
    public ResponseEntity<SuccessResponse<PublicProfileResponse>> getPublicProfile(
            @PathVariable Long userId
    ) {
        PublicProfileResponse response = memberService.getPublicProfile(userId);
        return ResponseEntity.ok(
                SuccessResponse.of(
                        "USER_200_PUBLIC_PROFILE_SUCCESS",
                        "사용자 공개 프로필 조회에 성공했습니다.",
                        response
                )
        );
    }

    @DeleteMapping("/me")
    public ResponseEntity<SuccessResponse<String>> withdraw(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        if (principal == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        memberService.withdraw(principal.userId());
        SecurityContextHolder.clearContext();

        ResponseCookie expiredCookie = ResponseCookie.from(accessCookieName, "")
                .httpOnly(true)
                .secure(accessCookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite(accessCookieSameSite)
                .build();

        SuccessCode successCode = SuccessCode.WITHDRAW_SUCCESS;
        return ResponseEntity
                .status(successCode.getStatus())
                .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                .body(SuccessResponse.of(successCode, "회원 탈퇴가 완료되었습니다."));
    }
}
