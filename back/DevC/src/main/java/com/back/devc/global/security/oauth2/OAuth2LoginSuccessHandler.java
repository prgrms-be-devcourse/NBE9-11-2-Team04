package com.back.devc.global.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Value("${custom.oauth2.frontend-success-url:http://localhost:3000/login}")
    private String frontendSuccessUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        if (!(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {
            String fallback = UriComponentsBuilder.fromUriString(frontendSuccessUrl)
                    .queryParam("oauth", "error")
                    .queryParam("reason", "invalid_principal")
                    .build(true)
                    .toUriString();

            response.sendRedirect(fallback);
            return;
        }

        String provider = "unknown";
        if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
            provider = oauth2Token.getAuthorizedClientRegistrationId();
        }

        String providerUserId = asString(oauth2User.getAttribute("id"));
        String login = asString(oauth2User.getAttribute("login"));
        String email = asString(oauth2User.getAttribute("email"));

        String redirectUrl = UriComponentsBuilder.fromUriString(frontendSuccessUrl)
                .queryParam("oauth", "success")
                .queryParam("provider", provider)
                .queryParam("providerUserId", providerUserId)
                .queryParam("login", login)
                .queryParam("email", email)
                .build(true)
                .toUriString();

        response.sendRedirect(redirectUrl);
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
