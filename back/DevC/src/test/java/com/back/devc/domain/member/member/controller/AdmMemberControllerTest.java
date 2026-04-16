package com.back.devc.domain.member.member.controller;

import com.back.devc.domain.member.member.dto.AdmMemberDetailResponse;
import com.back.devc.domain.member.member.dto.AdmMemberListResponse;
import com.back.devc.domain.member.member.dto.AdmMemberStatusUpdateRequest;
import com.back.devc.domain.member.member.entity.MemberStatus;
import com.back.devc.domain.member.member.service.AdmMemberService;
import com.back.devc.global.response.SuccessCode;
import com.back.devc.global.security.jwt.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
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
@DisplayName("AdmMemberController 테스트")
@AutoConfigureMockMvc(addFilters = false) // 시큐리티 필터는 여전히 비활성화
class AdmMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdmMemberService admMemberService;

    // --- 보안 필터 관련 의존성 Mocking (중요!) ---
    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private com.back.devc.domain.member.member.repository.MemberRepository memberRepository;

    @MockitoBean
    private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMetamodelMappingContext;
    // ------------------------------------------

    @Test
    void getMembers_success() throws Exception {
        AdmMemberListResponse dto = new AdmMemberListResponse(1L, "nick", "test@test.com", null);
        Page<AdmMemberListResponse> page = new PageImpl<>(List.of(dto));

        given(admMemberService.getMembers(anyInt(), anyInt())).willReturn(page);

        mockMvc.perform(get("/api/admin/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.MEMBER_LIST_SUCCESS.getCode()));
    }

    @Test
    void getMemberDetail_success() throws Exception {
        AdmMemberDetailResponse dto = new AdmMemberDetailResponse(
                1L, "test@test.com", "nick", 0L, 0L, null, LocalDateTime.now()
        );

        given(admMemberService.getMemberDetail(1L)).willReturn(dto);

        mockMvc.perform(get("/api/admin/members/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(1L));
    }

    @Test
    void updateMemberStatus_success() throws Exception {
        // 1. 유효한 상태값을 담은 요청 객체 생성 (Enum 또는 String에 맞는 값 입력)
        // 예: MemberStatus.ACTIVE 또는 "ACTIVE"
        AdmMemberStatusUpdateRequest request = new AdmMemberStatusUpdateRequest(MemberStatus.ACTIVE);

        AdmMemberDetailResponse response = new AdmMemberDetailResponse(
                1L, "test@test.com", "nick", 0L, 0L, MemberStatus.ACTIVE, LocalDateTime.now()
        );

        given(admMemberService.updateMemberStatus(eq(1L), any())).willReturn(response);

        // 2. 요청 실행
        mockMvc.perform(patch("/api/admin/members/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))) // 이제 status가 포함된 JSON이 전달됨
                .andExpect(status().isOk()) // 200 OK 기대
                .andExpect(jsonPath("$.code").value(SuccessCode.MEMBER_STATUS_UPDATE_SUCCESS.getCode()));
    }

    @Test
    void searchMembers_success() throws Exception {
        AdmMemberListResponse dto = new AdmMemberListResponse(1L, "nick", "test@test.com", null);
        Page<AdmMemberListResponse> page = new PageImpl<>(List.of(dto));

        given(admMemberService.searchMembers(eq("nick"), anyInt(), anyInt())).willReturn(page);

        mockMvc.perform(get("/api/admin/members/search")
                        .param("keyword", "nick") // 키워드가 필수인지 확인
                        .param("page", "0")       // 페이지 번호도 필요한지 확인
                        .param("size", "10"))
                .andExpect(status().isOk());
    }
}