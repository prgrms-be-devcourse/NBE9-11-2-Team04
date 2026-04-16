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

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuth2MemberService {

    private static final String GITHUB_EMAIL_DOMAIN = "@users.noreply.github.com";

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public OAuthPendingSignup buildGithubPendingSignup(OAuth2User oauth2User) {
        String providerUserId = valueAsString(oauth2User.getAttribute("id")).trim();
        if (providerUserId.isBlank()) {
            throw new IllegalStateException("GitHub 사용자 id를 찾을 수 없습니다.");
        }

        String login = valueAsString(oauth2User.getAttribute("login")).trim();
        String email = valueAsString(oauth2User.getAttribute("email")).trim();

        return new OAuthPendingSignup("github", providerUserId, email, login);
    }

    public Optional<Member> findGithubMemberByProviderUserId(String providerUserId) {
        return memberRepository.findByProviderAndProviderUserId(AuthProvider.GITHUB, providerUserId);
    }

    @Transactional
    public Member completeGithubSignup(OAuthPendingSignup pending, String nickname) {
        String normalizedNickname = nickname == null ? "" : nickname.trim();
        if (normalizedNickname.isBlank()) {
            throw new ApiException(ErrorCode.BAD_REQUEST);
        }

        if (memberRepository.existsByNickname(normalizedNickname)) {
            throw new ApiException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        // 이미 생성된 경우에는 기존 멤버 반환
        Optional<Member> existing = memberRepository.findByProviderAndProviderUserId(
                AuthProvider.GITHUB, pending.providerUserId()
        );
        if (existing.isPresent()) {
            return existing.get();
        }

        String resolvedEmail = resolveUniqueEmail(pending.emailFromProvider(), pending.providerUserId());
        String encodedPassword = passwordEncoder.encode(UUID.randomUUID().toString());

        Member newMember = Member.createOAuthMember(
                AuthProvider.GITHUB,
                pending.providerUserId(),
                resolvedEmail,
                encodedPassword,
                normalizedNickname
        );

        return memberRepository.save(newMember);
    }

    private String resolveUniqueEmail(String emailFromGithub, String providerUserId) {
        if (emailFromGithub != null && !emailFromGithub.isBlank() && !memberRepository.existsByEmail(emailFromGithub)) {
            return emailFromGithub;
        }

        String baseLocalPart = "github_" + providerUserId;
        String candidate = baseLocalPart + GITHUB_EMAIL_DOMAIN;
        int sequence = 1;

        while (memberRepository.existsByEmail(candidate)) {
            candidate = baseLocalPart + "_" + sequence + GITHUB_EMAIL_DOMAIN;
            sequence++;
        }

        return candidate;
    }

    private String valueAsString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
