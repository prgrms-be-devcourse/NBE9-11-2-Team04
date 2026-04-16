package com.back.devc.domain.auth.dto.oauth;

public record OAuthSignupCompleteResponse(
        String email,
        String nickname
) {
}
