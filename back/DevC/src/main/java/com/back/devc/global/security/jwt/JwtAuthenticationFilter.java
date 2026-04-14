package com.back.devc.global.security.jwt;

import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.entity.MemberStatus;
import com.back.devc.domain.member.member.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;

    // 모든 요청마다 1회 실행되며, JWT가 유효하면 인증 정보를 SecurityContext에 저장한다.
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // Authorization 헤더에서 Bearer 토큰 추출
        String token = resolveToken(request);

        // 이미 인증된 요청이 아니고, Access Token이 유효한 경우에만 인증 시도
        if (token != null
                && SecurityContextHolder.getContext().getAuthentication() == null
                && jwtProvider.validateAccessTokenStatus(token) == TokenValidationStatus.VALID) {
            Long userId = jwtProvider.getUserId(token);

            // 토큰 클레임만 신뢰하지 않고 DB에서 사용자 현재 상태를 다시 확인
            Member member = memberRepository.findById(userId).orElse(null);
            if (!isAuthenticatableMember(member)) {
                // 미인증 상태로 남겨두면, 보호된 API 접근 시 Security가 401로 처리한다.
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            String email = member.getEmail();
            String role = member.getRole().name();

            JwtPrincipal principal = new JwtPrincipal(userId, email, role);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    // 현재 정책: 사용자 존재 + BLACKLISTED 아님 => 인증 허용
    private boolean isAuthenticatableMember(Member member) {
        return member != null && member.getStatus() != MemberStatus.BLACKLISTED;
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }

        return authorization.substring(BEARER_PREFIX.length());
    }
}
