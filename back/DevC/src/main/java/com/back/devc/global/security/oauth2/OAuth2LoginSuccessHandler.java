package com.back.devc.global.security.oauth2;

import com.back.devc.domain.auth.service.OAuth2MemberService;
import com.back.devc.domain.member.member.entity.Member;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2MemberService oAuth2MemberService;

    @Value("${custom.oauth2.frontend-success-url:http://localhost:3000/login}")
    private String frontendSuccessUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        if (!(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {
            response.sendRedirect(errorRedirect("invalid_principal"));
            return;
        }

        String provider = "unknown";
        if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
            provider = oauth2Token.getAuthorizedClientRegistrationId();
        }

        try {
            Member member = oAuth2MemberService.getOrCreateGithubMember(oauth2User);

            String redirectUrl = UriComponentsBuilder.fromUriString(frontendSuccessUrl)
                    .queryParam("oauth", "success")
                    .queryParam("provider", provider)
                    .queryParam("userId", member.getUserId())
                    .queryParam("email", member.getEmail())
                    .queryParam("nickname", member.getNickname())
                    .build(true)
                    .toUriString();

            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            response.sendRedirect(errorRedirect("member_sync_failed"));
        }
    }

    private String errorRedirect(String reason) {
        return UriComponentsBuilder.fromUriString(frontendSuccessUrl)
                .queryParam("oauth", "error")
                .queryParam("reason", reason)
                .build(true)
                .toUriString();
    }
}
