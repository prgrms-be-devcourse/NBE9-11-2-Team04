package com.back.devc.domain.auth.service;

import com.back.devc.domain.auth.dto.login.LoginRequest;
import com.back.devc.domain.auth.dto.login.LoginResponse;
import com.back.devc.domain.auth.dto.logout.LogoutResponse;
import com.back.devc.domain.auth.dto.signup.SignUpRequest;
import com.back.devc.domain.auth.dto.signup.SignUpResponse;
import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.entity.MemberStatus;
import com.back.devc.domain.member.member.repository.MemberRepository;
import com.back.devc.global.exception.ApiException;
import com.back.devc.global.exception.ErrorCode;
import com.back.devc.global.security.jwt.JwtProvider;
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

    // 로그아웃 성공 응답 DTO를 생성한다. (서버 상태 변경 없음)
    @Transactional(readOnly = true)
    public LogoutResponse logout() {
        return LogoutResponse.success();
    }

    // 사용자 인증 정보를 검증하고 JWT를 발급해 로그인 응답 DTO를 반환한다.
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(ErrorCode.EMAIL_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
            throw new ApiException(ErrorCode.PASSWORD_MISMATCH);
        }

        if (member.getStatus() == MemberStatus.BLACKLISTED) {
            throw new ApiException(ErrorCode.MEMBER_BLACKLISTED);
        }

        String accessToken = jwtProvider.createAccessToken(member);

        return new LoginResponse(
                member.getUserId(),
                member.getEmail(),
                member.getNickname(),
                member.getRole(),
                member.getStatus(),
                accessToken
        );
    }

    // 이메일/닉네임 중복을 검사한 뒤 로컬 회원을 생성하고 회원가입 응답 DTO를 반환한다.
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
}
