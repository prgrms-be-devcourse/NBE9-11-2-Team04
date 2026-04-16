package com.back.devc.domain.auth.service;

import com.back.devc.domain.auth.dto.oauth.OAuthPendingSignup;
import com.back.devc.domain.member.member.entity.AuthProvider;
import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.repository.MemberRepository;
import com.back.devc.global.exception.ApiException;
import com.back.devc.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuth2MemberService {

    private static final String GITHUB_EMAIL_DOMAIN = "@users.noreply.github.com";
    private static final String KAKAO_EMAIL_DOMAIN = "@users.noreply.kakao.com";

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public OAuthPendingSignup buildPendingSignup(String provider, OAuth2User oauth2User) {
        String normalized = normalizeProvider(provider);

        if ("github".equals(normalized)) {
            return buildGithubPendingSignup(oauth2User);
        }

        if ("kakao".equals(normalized)) {
            return buildKakaoPendingSignup(oauth2User);
        }

        throw new ApiException(ErrorCode.OAUTH2_UNSUPPORTED_PROVIDER);
    }

    public Optional<Member> findMemberByProviderUserId(String provider, String providerUserId) {
        AuthProvider authProvider = toAuthProvider(provider);
        return memberRepository.findByProviderAndProviderUserId(authProvider, providerUserId);
    }

    public OAuthPendingSignup buildGithubPendingSignup(OAuth2User oauth2User) {
        String providerUserId = valueAsString(oauth2User.getAttribute("id")).trim();
        if (providerUserId.isBlank()) {
            throw new ApiException(ErrorCode.OAUTH2_PROVIDER_USER_ID_MISSING);
        }

        String login = valueAsString(oauth2User.getAttribute("login")).trim();
        String email = valueAsString(oauth2User.getAttribute("email")).trim();

        return new OAuthPendingSignup("github", providerUserId, email, login);
    }

    public OAuthPendingSignup buildKakaoPendingSignup(OAuth2User oauth2User) {
        String providerUserId = valueAsString(oauth2User.getAttribute("id")).trim();
        if (providerUserId.isBlank()) {
            throw new ApiException(ErrorCode.OAUTH2_PROVIDER_USER_ID_MISSING);
        }

        String email = "";
        String login = "";

        Object accountRaw = oauth2User.getAttribute("kakao_account");
        if (accountRaw instanceof Map<?, ?> accountMap) {
            email = valueAsString(accountMap.get("email")).trim();

            Object profileRaw = accountMap.get("profile");
            if (profileRaw instanceof Map<?, ?> profileMap) {
                login = valueAsString(profileMap.get("nickname")).trim();
            }
        }

        return new OAuthPendingSignup("kakao", providerUserId, email, login);
    }

    @Transactional
    public Member completeGithubSignup(OAuthPendingSignup pending, String nickname) {
        return completeSignupByProvider(AuthProvider.GITHUB, pending, nickname, GITHUB_EMAIL_DOMAIN, "github_");
    }

    @Transactional
    public Member completeKakaoSignup(OAuthPendingSignup pending, String nickname) {
        return completeSignupByProvider(AuthProvider.KAKAO, pending, nickname, KAKAO_EMAIL_DOMAIN, "kakao_");
    }

    private Member completeSignupByProvider(
            AuthProvider provider,
            OAuthPendingSignup pending,
            String nickname,
            String fallbackEmailDomain,
            String localPrefix
    ) {
        if (pending == null || pending.providerUserId() == null || pending.providerUserId().isBlank()) {
            throw new ApiException(ErrorCode.OAUTH2_PENDING_SIGNUP_REQUIRED);
        }

        Optional<Member> existing = memberRepository.findByProviderAndProviderUserId(
                provider, pending.providerUserId()
        );
        if (existing.isPresent()) {
            return existing.get();
        }

        String normalizedNickname = nickname == null ? "" : nickname.trim();
        if (normalizedNickname.isBlank()) {
            throw new ApiException(ErrorCode.BAD_REQUEST);
        }

        if (memberRepository.existsByNickname(normalizedNickname)) {
            throw new ApiException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        String resolvedEmail = resolveUniqueEmail(
                pending.emailFromProvider(),
                pending.providerUserId(),
                fallbackEmailDomain,
                localPrefix
        );
        String encodedPassword = passwordEncoder.encode(UUID.randomUUID().toString());

        Member newMember = Member.createOAuthMember(
                provider,
                pending.providerUserId(),
                resolvedEmail,
                encodedPassword,
                normalizedNickname
        );

        return memberRepository.save(newMember);
    }

    private AuthProvider toAuthProvider(String provider) {
        String normalized = normalizeProvider(provider);

        if ("github".equals(normalized)) {
            return AuthProvider.GITHUB;
        }

        if ("kakao".equals(normalized)) {
            return AuthProvider.KAKAO;
        }

        throw new ApiException(ErrorCode.OAUTH2_UNSUPPORTED_PROVIDER);
    }

    private String normalizeProvider(String provider) {
        return provider == null ? "" : provider.trim().toLowerCase(Locale.ROOT);
    }

    private String resolveUniqueEmail(
            String emailFromProvider,
            String providerUserId,
            String fallbackDomain,
            String localPrefix
    ) {
        if (emailFromProvider != null
                && !emailFromProvider.isBlank()
                && !memberRepository.existsByEmail(emailFromProvider)) {
            return emailFromProvider;
        }

        String baseLocalPart = localPrefix + providerUserId;
        String candidate = baseLocalPart + fallbackDomain;
        int sequence = 1;

        while (memberRepository.existsByEmail(candidate)) {
            candidate = baseLocalPart + "_" + sequence + fallbackDomain;
            sequence++;
        }

        return candidate;
    }

    private String valueAsString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
