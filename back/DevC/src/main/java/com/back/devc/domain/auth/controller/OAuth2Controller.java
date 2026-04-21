package com.back.devc.domain.auth.controller;

import com.back.devc.domain.auth.dto.OAuth2MeResponse;
import com.back.devc.domain.auth.dto.login.LoginResponse;
import com.back.devc.domain.auth.dto.oauth.OAuthExchangeRequest;
import com.back.devc.domain.auth.dto.oauth.OAuthPendingSignup;
import com.back.devc.domain.auth.dto.oauth.OAuthSignupCompleteRequest;
import com.back.devc.domain.auth.service.OAuth2MemberService;
import com.back.devc.global.exception.ApiException;
import com.back.devc.global.exception.ErrorCode;
import com.back.devc.global.response.SuccessCode;
import com.back.devc.global.response.SuccessResponse;
import com.back.devc.global.security.jwt.AuthCookieService;
import com.back.devc.global.security.oauth2.OAuth2LoginSuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/oauth2")
public class OAuth2Controller {

    private final OAuth2MemberService oAuth2MemberService;
    private final AuthCookieService authCookieService;

    @Value("${custom.jwt.access-token-expiration-seconds:3600}")
    private long accessTokenExpirationSeconds;

    @GetMapping("/me")
    public OAuth2MeResponse me(
            @AuthenticationPrincipal OAuth2User oauth2User,
            HttpServletRequest httpServletRequest
    ) {
        HttpSession session = httpServletRequest.getSession(false);
        if (session != null) {
            Object raw = session.getAttribute(OAuth2LoginSuccessHandler.PENDING_SIGNUP_SESSION_KEY);
            if (raw instanceof OAuthPendingSignup pending) {
                Map<String, Object> attributes = new LinkedHashMap<>();
                attributes.put("pendingSignup", true);
                attributes.put("provider", pending.provider());
                attributes.put("providerUserId", pending.providerUserId());
                attributes.put("email", pending.emailFromProvider());
                attributes.put("login", pending.loginFromProvider());

                return new OAuth2MeResponse(false, null, List.of(), attributes);
            }
        }

        if (oauth2User == null) {
            return new OAuth2MeResponse(false, null, List.of(), Map.of("pendingSignup", false));
        }

        List<String> authorities = oauth2User.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Map<String, Object> attributes = new LinkedHashMap<>(oauth2User.getAttributes());
        attributes.put("pendingSignup", false);

        return new OAuth2MeResponse(true, oauth2User.getName(), authorities, attributes);
    }

    @PostMapping("/exchange")
    public ResponseEntity<SuccessResponse<LoginResponse>> exchange(
            @Valid @RequestBody OAuthExchangeRequest request,
            HttpServletResponse response
    ) {
        LoginResponse body = oAuth2MemberService.exchangeLoginCode(request.code());
        authCookieService.setAccessTokenCookie(response, body.accessToken(), accessTokenExpirationSeconds);

        SuccessCode successCode = SuccessCode.LOGIN_SUCCESS;
        return ResponseEntity
                .status(successCode.getStatus())
                .body(SuccessResponse.of(successCode, body));
    }

    @PostMapping("/signup/complete")
    public ResponseEntity<SuccessResponse<LoginResponse>> completeSignup(
            @Valid @RequestBody OAuthSignupCompleteRequest request,
            HttpServletRequest httpServletRequest,
            HttpServletResponse response
    ) {
        HttpSession session = httpServletRequest.getSession(false);
        if (session == null) {
            throw new ApiException(ErrorCode.OAUTH2_PENDING_SIGNUP_EXPIRED);
        }

        Object raw = session.getAttribute(OAuth2LoginSuccessHandler.PENDING_SIGNUP_SESSION_KEY);
        if (!(raw instanceof OAuthPendingSignup pending)) {
            throw new ApiException(ErrorCode.OAUTH2_PENDING_SIGNUP_REQUIRED);
        }

        LoginResponse body = oAuth2MemberService.completeSignupAndIssueToken(pending, request.nickname());
        session.removeAttribute(OAuth2LoginSuccessHandler.PENDING_SIGNUP_SESSION_KEY);
        authCookieService.setAccessTokenCookie(response, body.accessToken(), accessTokenExpirationSeconds);

        SuccessCode successCode = SuccessCode.SIGN_UP_SUCCESS;
        return ResponseEntity
                .status(successCode.getStatus())
                .body(SuccessResponse.of(successCode, body));
    }
}