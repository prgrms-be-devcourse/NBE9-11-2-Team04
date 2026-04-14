package com.back.devc.global.security.jwt;

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

    // 모든 요청마다 1회 실행되며, JWT가 유효하면 인증 정보를 SecurityContext에 저장한다.
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // Authorization 헤더에서 Bearer 토큰 추출
        String token = resolveToken(request);

        // 토큰이 있고 유효하며, 아직 인증 정보가 없을 때만 인증 객체를 생성한다.
        if (token != null && jwtProvider.validateToken(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 토큰의 클레임에서 사용자 식별 정보 추출
            Long userId = jwtProvider.getUserId(token);
            String email = jwtProvider.getEmail(token);
            String role = jwtProvider.getRole(token);

            // 인증 주체(principal) 생성
            JwtPrincipal principal = new JwtPrincipal(userId, email, role);
            // Spring Security 인증 객체 생성 (권한은 ROLE_ 접두어 사용)
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );

            // 현재 요청의 인증 컨텍스트에 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    // Authorization: Bearer <token> 형식에서 JWT 문자열만 추출한다.
    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }

        return authorization.substring(BEARER_PREFIX.length());
    }
}
