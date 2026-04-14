package com.back.devc.domain.interaction.notification.dto;

import java.util.List;

public class NotificationListResponse {

    private final List<NotificationResponse> notifications;

    public NotificationListResponse(List<NotificationResponse> notifications) {
        this.notifications = notifications;
    }

    public List<NotificationResponse> getNotifications() {
        return notifications;
    }
}