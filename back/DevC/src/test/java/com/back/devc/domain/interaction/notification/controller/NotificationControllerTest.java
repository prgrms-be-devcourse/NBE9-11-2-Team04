package com.back.devc.domain.interaction.notification.controller;

import com.back.devc.domain.interaction.notification.dto.NotificationListResponse;
import com.back.devc.domain.interaction.notification.dto.NotificationResponse;
import com.back.devc.domain.interaction.notification.service.NotificationService;
import com.back.devc.global.security.jwt.JwtProvider;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("내 알림 목록 조회 API 호출 성공")
    void getMyNotifications_success() throws Exception {
        NotificationListResponse response = org.mockito.Mockito.mock(NotificationListResponse.class);

        given(notificationService.getMyNotifications(1L)).willReturn(response);

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk());

        verify(notificationService).getMyNotifications(1L);
    }

    @Test
    @DisplayName("알림 읽음 처리 API 호출 성공")
    void readNotification_success() throws Exception {
        NotificationResponse response = org.mockito.Mockito.mock(NotificationResponse.class);

        given(notificationService.readNotification(1L, 1L)).willReturn(response);

        mockMvc.perform(patch("/api/notifications/{notificationId}/read", 1L))
                .andExpect(status().isOk());

        verify(notificationService).readNotification(1L, 1L);
    }
}
