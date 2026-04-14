package com.back.devc.domain.interaction.notification.controller;

import com.back.devc.domain.interaction.notification.dto.NotificationListResponse;
import com.back.devc.domain.interaction.notification.dto.NotificationResponse;
import com.back.devc.domain.interaction.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<NotificationListResponse> getMyNotifications() {
        Long loginUserId = 1L;
        return ResponseEntity.ok(notificationService.getMyNotifications(loginUserId));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> readNotification(@PathVariable Long notificationId) {
        Long loginUserId = 1L;
        return ResponseEntity.ok(notificationService.readNotification(notificationId, loginUserId));
    }
}