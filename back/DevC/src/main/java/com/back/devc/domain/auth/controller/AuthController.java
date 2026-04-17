package com.back.devc.domain.auth.controller;

import com.back.devc.domain.auth.dto.login.LoginRequest;
import com.back.devc.domain.auth.dto.login.LoginResponse;
import com.back.devc.domain.auth.dto.logout.LogoutResponse;
import com.back.devc.domain.auth.dto.signup.SignUpRequest;
import com.back.devc.domain.auth.dto.signup.SignUpResponse;
import com.back.devc.domain.auth.service.AuthService;
import com.back.devc.global.response.SuccessCode;
import com.back.devc.global.response.SuccessResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Value("${custom.jwt.access-cookie-name:access_token}")
    private String accessCookieName;

    @Value("${custom.jwt.access-cookie-secure:false}")
    private boolean accessCookieSecure;

    @Value("${custom.jwt.access-cookie-same-site:Lax}")
    private String accessCookieSameSite;

    @Value("${custom.jwt.access-token-expiration-seconds:3600}")
    private long accessTokenExpirationSeconds;

    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse<LogoutResponse>> logout(HttpServletResponse response) {
        LogoutResponse body = authService.logout();
        setAccessTokenCookie(response, "", 0);

        SuccessCode successCode = SuccessCode.LOGOUT_SUCCESS;
        return ResponseEntity
                .status(successCode.getStatus())
                .body(SuccessResponse.of(successCode, body));
    }

    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        LoginResponse body = authService.login(request);

        // Bearer + HttpOnly cookie 동시 지원 (프론트 새로고침 안정화)
        setAccessTokenCookie(response, body.accessToken(), accessTokenExpirationSeconds);

        SuccessCode successCode = SuccessCode.LOGIN_SUCCESS;
        return ResponseEntity
                .status(successCode.getStatus())
                .body(SuccessResponse.of(successCode, body));
    }

    @PostMapping("/signup")
    public ResponseEntity<SuccessResponse<SignUpResponse>> signUp(@Valid @RequestBody SignUpRequest request) {
        SignUpResponse body = authService.signUp(request);
        SuccessCode successCode = SuccessCode.SIGN_UP_SUCCESS;
        return ResponseEntity
                .status(successCode.getStatus())
                .body(SuccessResponse.of(successCode, body));
    }

    private void setAccessTokenCookie(HttpServletResponse response, String token, long maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from(accessCookieName, token == null ? "" : token)
                .httpOnly(true)
                .secure(accessCookieSecure)
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite(accessCookieSameSite)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}