package com.back.devc.domain.member.member.controller;

import com.back.devc.domain.member.member.dto.AdmMemberDetailResponse;
import com.back.devc.domain.member.member.dto.AdmMemberListResponse;
import com.back.devc.domain.member.member.dto.AdmMemberStatusUpdateRequest;
import com.back.devc.domain.member.member.entity.MemberStatus;
import com.back.devc.domain.member.member.service.AdmMemberService;
import com.back.devc.global.response.SuccessCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdmMemberController.class)
@WithMockUser(roles = "ADMIN") // 관리자 권한 시뮬레이션
@DisplayName("AdmMemberController 테스트")
class AdmMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdmMemberService admMemberService;

    @Test
    @DisplayName("[성공] 전체 회원 목록 조회")
    void getMembers_Success() throws Exception {
        // given
        AdmMemberListResponse memberDto = new AdmMemberListResponse(1L, "nickname", "test@test.com", MemberStatus.ACTIVE);
        Page<AdmMemberListResponse> pageResponse = new PageImpl<>(List.of(memberDto));

        given(admMemberService.getMembers(anyInt(), anyInt())).willReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/admin/members")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.MEMBER_LIST_SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.content[0].nickname").value("nickname"));
    }

    @Test
    @DisplayName("[성공] 회원 상세 조회")
    void getMemberDetail_Success() throws Exception {
        // given
        AdmMemberDetailResponse detailResponse = new AdmMemberDetailResponse(1L, "nickname", "test@test.com", MemberStatus.ACTIVE, 0);
        given(admMemberService.getMemberDetail(1L)).willReturn(detailResponse);

        // when & then
        mockMvc.perform(get("/api/admin/members/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.MEMBER_DETAIL_SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.userId").value(1L));
    }

    @Test
    @DisplayName("[성공] 회원 상태 변경")
    void updateMemberStatus_Success() throws Exception {
        // given
        AdmMemberStatusUpdateRequest request = new AdmMemberStatusUpdateRequest(MemberStatus.BLACKLISTED);
        AdmMemberDetailResponse detailResponse = new AdmMemberDetailResponse(1L, "nickname", "test@test.com", MemberStatus.BLACKLISTED, 0);

        given(admMemberService.updateMemberStatus(eq(1L), any(AdmMemberStatusUpdateRequest.class)))
                .willReturn(detailResponse);

        // when & then
        mockMvc.perform(patch("/api/admin/members/1/status")
                        .with(csrf()) // Spring Security 사용 시 필요
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.MEMBER_STATUS_UPDATE_SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.status").value("BLACKLISTED"));
    }

    @Test
    @DisplayName("[성공] 회원 검색")
    void searchMembers_Success() throws Exception {
        // given
        AdmMemberListResponse memberDto = new AdmMemberListResponse(1L, "nickname", "test@test.com", MemberStatus.ACTIVE);
        Page<AdmMemberListResponse> pageResponse = new PageImpl<>(List.of(memberDto));

        given(admMemberService.searchMembers(eq("nickname"), anyInt(), anyInt())).willReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/admin/members/search")
                        .param("keyword", "nickname"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.MEMBER_SEARCH_SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.content[0].userId").value(1L));
    }
}