package com.back.devc.domain.auth.controller;

import com.back.devc.domain.auth.dto.OAuth2MeResponse;
import com.back.devc.domain.auth.dto.oauth.OAuthPendingSignup;
import com.back.devc.domain.auth.dto.oauth.OAuthSignupCompleteRequest;
import com.back.devc.domain.auth.dto.oauth.OAuthSignupCompleteResponse;
import com.back.devc.domain.auth.service.OAuth2MemberService;
import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.global.exception.ApiException;
import com.back.devc.global.exception.ErrorCode;
import com.back.devc.global.response.SuccessCode;
import com.back.devc.global.response.SuccessResponse;
import com.back.devc.global.security.jwt.JwtProvider;
import com.back.devc.global.security.oauth2.OAuth2LoginSuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/oauth2")
public class OAuth2Controller {

    private final OAuth2MemberService oAuth2MemberService;
    private final JwtProvider jwtProvider;

    @Value("${custom.jwt.access-token-expiration-seconds:3600}")
    private long accessTokenExpirationSeconds;

    @Value("${custom.jwt.access-cookie-name:access_token}")
    private String accessCookieName;

    @Value("${custom.jwt.access-cookie-secure:false}")
    private boolean accessCookieSecure;

    @Value("${custom.jwt.access-cookie-same-site:Lax}")
    private String accessCookieSameSite;

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

    @PostMapping("/signup/complete")
    public ResponseEntity<SuccessResponse<OAuthSignupCompleteResponse>> completeSignup(
            @Valid @RequestBody OAuthSignupCompleteRequest request,
            HttpServletRequest httpServletRequest
    ) {
        HttpSession session = httpServletRequest.getSession(false);
        if (session == null) {
            throw new ApiException(ErrorCode.OAUTH2_PENDING_SIGNUP_EXPIRED);
        }

        Object raw = session.getAttribute(OAuth2LoginSuccessHandler.PENDING_SIGNUP_SESSION_KEY);
        if (!(raw instanceof OAuthPendingSignup pending)) {
            throw new ApiException(ErrorCode.OAUTH2_PENDING_SIGNUP_REQUIRED);
        }

        Member member = completeByProvider(pending, request.nickname());

        String accessToken = jwtProvider.createAccessToken(member);
        ResponseCookie accessCookie = ResponseCookie.from(accessCookieName, accessToken)
                .httpOnly(true)
                .secure(accessCookieSecure)
                .path("/")
                .maxAge(accessTokenExpirationSeconds)
                .sameSite(accessCookieSameSite)
                .build();

        session.removeAttribute(OAuth2LoginSuccessHandler.PENDING_SIGNUP_SESSION_KEY);

        OAuthSignupCompleteResponse body = new OAuthSignupCompleteResponse(
                member.getEmail(),
                member.getNickname()
        );

        SuccessCode successCode = SuccessCode.SIGN_UP_SUCCESS;
        return ResponseEntity
                .status(successCode.getStatus())
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .body(SuccessResponse.of(successCode, body));
    }

    private Member completeByProvider(OAuthPendingSignup pending, String nickname) {
        String provider = pending.provider() == null ? "" : pending.provider().trim().toLowerCase(Locale.ROOT);

        if ("github".equals(provider)) {
            return oAuth2MemberService.completeGithubSignup(pending, nickname);
        } else if ("kakao".equals(provider)) {
            return oAuth2MemberService.completeKakaoSignup(pending, nickname);
        }

        throw new ApiException(ErrorCode.BAD_REQUEST);
    }

}