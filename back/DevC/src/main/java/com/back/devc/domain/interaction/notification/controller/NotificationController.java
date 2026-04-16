package com.back.devc.domain.interaction.notification.controller;

import com.back.devc.domain.interaction.notification.dto.NotificationListResponse;
import com.back.devc.domain.interaction.notification.dto.NotificationResponse;
import com.back.devc.domain.interaction.notification.service.NotificationService;
import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import com.back.devc.global.security.jwt.JwtPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;


@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final MemberRepository memberRepository;

    @GetMapping
    public ResponseEntity<NotificationListResponse> getMyNotifications(Authentication authentication) {
        return ResponseEntity.ok(notificationService.getMyNotifications(getAuthenticatedUserId(authentication)));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> readNotification(
            Authentication authentication,
            @PathVariable Long notificationId
    ) {
        return ResponseEntity.ok(notificationService.readNotification(notificationId, getAuthenticatedUserId(authentication)));
    }

    private Long getAuthenticatedUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof JwtPrincipal jwtPrincipal) {
            return jwtPrincipal.userId();
        }

        if (principal instanceof OAuth2User oAuth2User) {
            String email = oAuth2User.getAttribute("email");

            if (email == null || email.isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "OAuth 사용자 정보를 확인할 수 없습니다.");
            }

            Member member = memberRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "회원을 찾을 수 없습니다."));

            return member.getUserId();
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
    }
}