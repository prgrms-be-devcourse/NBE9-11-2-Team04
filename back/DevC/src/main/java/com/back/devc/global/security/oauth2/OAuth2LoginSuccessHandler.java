package com.back.devc.global.security.oauth2;

import com.back.devc.domain.auth.service.OAuth2MemberService;
import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.entity.MemberStatus;
import com.back.devc.global.security.jwt.JwtProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final String ERROR_INVALID_PRINCIPAL = "OAUTH2_INVALID_PRINCIPAL";
    private static final String ERROR_MEMBER_BLACKLISTED = "OAUTH2_MEMBER_BLACKLISTED";
    private static final String ERROR_TOKEN_ISSUE = "OAUTH2_TOKEN_ISSUE";

    private final OAuth2MemberService oAuth2MemberService;
    private final JwtProvider jwtProvider;
    private final OAuth2RedirectUrlResolver redirectUrlResolver;

    @Value("${custom.jwt.access-token-expiration-seconds:3600}")
    private long accessTokenExpirationSeconds;

    @Value("${custom.jwt.access-cookie-name:access_token}")
    private String accessCookieName;

    @Value("${custom.jwt.access-cookie-secure:false}")
    private boolean accessCookieSecure;

    @Value("${custom.jwt.access-cookie-same-site:Lax}")
    private String accessCookieSameSite;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        if (!(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {
            response.sendRedirect(redirectUrlResolver.buildFailureUrl(ERROR_INVALID_PRINCIPAL));
            return;
        }

        String provider = "unknown";
        if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
            provider = oauth2Token.getAuthorizedClientRegistrationId();
        }

        try {
            Member member = oAuth2MemberService.getOrCreateGithubMember(oauth2User);

            if (member.getStatus() == MemberStatus.BLACKLISTED) {
                response.sendRedirect(redirectUrlResolver.buildFailureUrl(ERROR_MEMBER_BLACKLISTED));
                return;
            }

            String accessToken = jwtProvider.createAccessToken(member);

            ResponseCookie accessCookie = ResponseCookie.from(accessCookieName, accessToken)
                    .httpOnly(true)
                    .secure(accessCookieSecure)
                    .path("/")
                    .maxAge(accessTokenExpirationSeconds)
                    .sameSite(accessCookieSameSite)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
            response.sendRedirect(redirectUrlResolver.buildSuccessUrl(provider, member));
        } catch (Exception e) {
            response.sendRedirect(redirectUrlResolver.buildFailureUrl(ERROR_TOKEN_ISSUE));
        }
    }
}
