package com.back.devc.domain.auth.service;

import com.back.devc.domain.auth.dto.login.LoginRequest;
import com.back.devc.domain.auth.dto.login.LoginResponse;
import com.back.devc.domain.auth.dto.logout.LogoutRequest;
import com.back.devc.domain.auth.dto.logout.LogoutResponse;
import com.back.devc.domain.auth.dto.reissue.ReissueRequest;
import com.back.devc.domain.auth.dto.reissue.ReissueResponse;
import com.back.devc.domain.auth.dto.signup.SignUpRequest;
import com.back.devc.domain.auth.dto.signup.SignUpResponse;
import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.entity.MemberStatus;
import com.back.devc.domain.member.member.repository.MemberRepository;
import com.back.devc.global.exception.ApiException;
import com.back.devc.global.exception.ErrorCode;
import com.back.devc.global.security.jwt.JwtProvider;
import com.back.devc.global.security.jwt.TokenValidationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional(readOnly = true)
    public LogoutResponse logout(LogoutRequest request) {
        return new LogoutResponse("로그아웃이 완료되었습니다.");
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(ErrorCode.EMAIL_NOT_FOUND));

        if (member.getStatus() == MemberStatus.BLACKLISTED) {
            throw new ApiException(ErrorCode.MEMBER_BLACKLISTED);
        }

        if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
            throw new ApiException(ErrorCode.PASSWORD_MISMATCH);
        }

        String accessToken = jwtProvider.createAccessToken(member);
        String refreshToken = jwtProvider.createRefreshToken(member);

        return new LoginResponse(
                member.getUserId(),
                member.getEmail(),
                member.getNickname(),
                member.getRole(),
                member.getStatus(),
                accessToken,
                refreshToken
        );
    }

    @Transactional(readOnly = true)
    public ReissueResponse reissue(ReissueRequest request) {
        TokenValidationStatus tokenStatus = jwtProvider.validateRefreshTokenStatus(request.refreshToken());
        if (!tokenStatus.isValid()) {
            throw new ApiException(toTokenErrorCode(tokenStatus));
        }

        Long userId = jwtProvider.getUserId(request.refreshToken());
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getStatus() == MemberStatus.BLACKLISTED) {
            throw new ApiException(ErrorCode.MEMBER_BLACKLISTED);
        }

        String newAccessToken = jwtProvider.createAccessToken(member);
        return new ReissueResponse(newAccessToken);
    }

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new ApiException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (memberRepository.existsByNickname(request.nickname())) {
            throw new ApiException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        Member member = Member.createLocalMember(request.email(), encodedPassword, request.nickname());
        Member savedMember = memberRepository.save(member);

        return new SignUpResponse(
                savedMember.getUserId(),
                savedMember.getEmail(),
                savedMember.getNickname(),
                savedMember.getRole(),
                savedMember.getStatus()
        );
    }

    private ErrorCode toTokenErrorCode(TokenValidationStatus tokenStatus) {
        return switch (tokenStatus) {
            case EXPIRED -> ErrorCode.EXPIRED_TOKEN;
            case INVALID_TOKEN_TYPE -> ErrorCode.INVALID_TOKEN_TYPE;
            case MISSING, MALFORMED, UNSUPPORTED, INVALID_SIGNATURE -> ErrorCode.INVALID_TOKEN;
            case VALID -> ErrorCode.INVALID_TOKEN;
        };
    }
}
