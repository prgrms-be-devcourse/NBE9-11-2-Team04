package com.back.devc.domain.auth.service;

import com.back.devc.domain.member.member.entity.AuthProvider;
import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuth2MemberService {

    private static final int MAX_NICKNAME_LENGTH = 50;
    private static final String GITHUB_EMAIL_DOMAIN = "@users.noreply.github.com";

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Member getOrCreateGithubMember(OAuth2User oauth2User) {
        String providerUserId = valueAsString(oauth2User.getAttribute("id")).trim();

        if (providerUserId.isBlank()) {
            throw new IllegalStateException("GitHub 사용자 id를 찾을 수 없습니다.");
        }

        return memberRepository.findByProviderAndProviderUserId(AuthProvider.GITHUB, providerUserId)
                .orElseGet(() -> createGithubMember(oauth2User, providerUserId));
    }

    private Member createGithubMember(OAuth2User oauth2User, String providerUserId) {
        String login = valueAsString(oauth2User.getAttribute("login"));
        String email = valueAsString(oauth2User.getAttribute("email"));

        String resolvedEmail = resolveUniqueEmail(email, providerUserId);
        String resolvedNickname = resolveUniqueNickname(login, providerUserId);
        String encodedPassword = passwordEncoder.encode(UUID.randomUUID().toString());

        Member newMember = Member.createOAuthMember(
                AuthProvider.GITHUB,
                providerUserId,
                resolvedEmail,
                encodedPassword,
                resolvedNickname
        );

        return memberRepository.save(newMember);
    }

    private String resolveUniqueEmail(String emailFromGithub, String providerUserId) {
        if (!emailFromGithub.isBlank() && !memberRepository.existsByEmail(emailFromGithub)) {
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

    private String resolveUniqueNickname(String login, String providerUserId) {
        String base = sanitizeNickname(login);
        if (base.isBlank()) {
            base = "github_" + providerUserId;
        }
        base = trimToMax(base, MAX_NICKNAME_LENGTH);

        if (!memberRepository.existsByNickname(base)) {
            return base;
        }

        int sequence = 1;
        while (true) {
            String suffix = "_" + sequence;
            String prefix = trimToMax(base, MAX_NICKNAME_LENGTH - suffix.length());
            String candidate = prefix + suffix;

            if (!memberRepository.existsByNickname(candidate)) {
                return candidate;
            }

            sequence++;
        }
    }

    private String sanitizeNickname(String value) {
        if (value == null) {
            return "";
        }

        return value
                .trim()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z0-9._-]", "");
    }

    private String trimToMax(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String valueAsString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}