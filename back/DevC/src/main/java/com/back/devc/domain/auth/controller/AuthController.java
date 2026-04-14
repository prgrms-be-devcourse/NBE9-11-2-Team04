package com.back.devc.domain.auth.controller;

import com.back.devc.domain.auth.dto.login.LoginRequest;
import com.back.devc.domain.auth.dto.login.LoginResponse;
import com.back.devc.domain.auth.dto.logout.LogoutRequest;
import com.back.devc.domain.auth.dto.logout.LogoutResponse;
import com.back.devc.domain.auth.dto.signup.SignUpRequest;
import com.back.devc.domain.auth.dto.signup.SignUpResponse;
import com.back.devc.domain.auth.service.AuthService;
import com.back.devc.global.response.SuccessCode;
import com.back.devc.global.response.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse<LogoutResponse>> logout(@RequestBody(required = false) LogoutRequest request) {
        LogoutResponse response = authService.logout(request);
        SuccessCode successCode = SuccessCode.LOGOUT_SUCCESS;
        return ResponseEntity
                .status(successCode.getStatus())
                .body(SuccessResponse.of(successCode, response));
    }

    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        SuccessCode successCode = SuccessCode.LOGIN_SUCCESS;
        return ResponseEntity
                .status(successCode.getStatus())
                .body(SuccessResponse.of(successCode, response));
    }

    @PostMapping("/signup")
    public ResponseEntity<SuccessResponse<SignUpResponse>> signUp(@Valid @RequestBody SignUpRequest request) {
        SignUpResponse response = authService.signUp(request);
        SuccessCode successCode = SuccessCode.SIGN_UP_SUCCESS;
        return ResponseEntity
                .status(successCode.getStatus())
                .body(SuccessResponse.of(successCode, response));
    }
}
