package com.back.devc.domain.member.member.controller;

import com.back.devc.domain.member.member.dto.AdmMemberDetailResponse;
import com.back.devc.domain.member.member.dto.AdmMemberListResponse;
import com.back.devc.domain.member.member.dto.AdmMemberStatusUpdateRequest;
import com.back.devc.domain.member.member.service.AdmMemberService;
import com.back.devc.global.response.SuccessCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdmMemberController.class)
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN")
@DisplayName("AdmMemberController 테스트")
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(exclude = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
})
class AdmMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdmMemberService admMemberService;

    // 1. 목록 조회
    @Test
    void getMembers_success() throws Exception {

        AdmMemberListResponse dto =
                new AdmMemberListResponse(1L, "nick", "test@test.com", null);

        Page<AdmMemberListResponse> page =
                new PageImpl<>(List.of(dto));

        given(admMemberService.getMembers(anyInt(), anyInt()))
                .willReturn(page);

        mockMvc.perform(get("/api/admin/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.MEMBER_LIST_SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.content[0].nickname").value("nick"));
    }

    // 2. 상세 조회
    @Test
    void getMemberDetail_success() throws Exception {

        AdmMemberDetailResponse dto =
                new AdmMemberDetailResponse(
                        1L, "test@test.com", "nick",
                        0L, 0L, null,
                        LocalDateTime.now()
                );

        given(admMemberService.getMemberDetail(1L))
                .willReturn(dto);

        mockMvc.perform(get("/api/admin/members/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.MEMBER_DETAIL_SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.userId").value(1L));
    }

    // 3. 상태 변경
    @Test
    void updateMemberStatus_success() throws Exception {

        AdmMemberStatusUpdateRequest request =
                new AdmMemberStatusUpdateRequest(null);

        AdmMemberDetailResponse response =
                new AdmMemberDetailResponse(
                        1L, "test@test.com", "nick",
                        0L, 0L, null,
                        LocalDateTime.now()
                );

        given(admMemberService.updateMemberStatus(eq(1L), any()))
                .willReturn(response);

        mockMvc.perform(patch("/api/admin/members/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.MEMBER_STATUS_UPDATE_SUCCESS.getCode()));
    }

    // 4. 검색
    @Test
    void searchMembers_success() throws Exception {

        AdmMemberListResponse dto =
                new AdmMemberListResponse(1L, "nick", "test@test.com", null);

        Page<AdmMemberListResponse> page =
                new PageImpl<>(List.of(dto));

        given(admMemberService.searchMembers(eq("nick"), anyInt(), anyInt()))
                .willReturn(page);

        mockMvc.perform(get("/api/admin/members/search")
                        .param("keyword", "nick"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.MEMBER_SEARCH_SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.content[0].nickname").value("nick"));
    }
}