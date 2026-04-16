package com.back.devc.domain.auth.dto.oauth;

public record OAuthSignupCompleteResponse(
        String accessToken,
        String email,
        String nickname
) {
}
