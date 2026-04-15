package com.back.devc.domain.auth.controller.member.mypage;

import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.repository.MemberRepository;
import com.back.devc.global.security.jwt.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MypageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JwtProvider jwtProvider;

    private Member createMember(String email, String nickname) {
        return memberRepository.save(
                Member.createLocalMember(email, "encoded-password", nickname)
        );
    }

    private String createAccessToken(Member member) {
        return jwtProvider.createAccessToken(member);
    }

    @Test
    @DisplayName("마이페이지 내 정보 조회 성공")
    void getMyProfile_success() throws Exception {
        Member member = createMember("test@test.com", "tester");
        String accessToken = createAccessToken(member);

        mockMvc.perform(
                        get("/users/me")
                                .header("Authorization", "Bearer " + accessToken)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(member.getUserId()))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.nickname").value("tester"));
    }

    @Test
    @DisplayName("내 정보 수정 - 닉네임 변경 성공")
    void updateMyProfile_success() throws Exception {
        Member member = createMember("test2@test.com", "oldNickname");
        String accessToken = createAccessToken(member);

        String requestBody = """
                {
                  "nickname": "newNickname"
                }
                """;

        mockMvc.perform(
                        patch("/users/me")
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(member.getUserId()))
                .andExpect(jsonPath("$.email").value("test2@test.com"))
                .andExpect(jsonPath("$.nickname").value("newNickname"));

        Member updatedMember = memberRepository.findById(member.getUserId()).orElseThrow();
        assertThat(updatedMember.getNickname()).isEqualTo("newNickname");
    }

    @Test
    @DisplayName("내 정보 수정 - 토큰 없으면 401")
    void updateMyProfile_unauthorized() throws Exception {
        String requestBody = """
                {
                  "nickname": "newNickname"
                }
                """;

        mockMvc.perform(
                        patch("/users/me")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("내 정보 수정 - 잘못된 토큰이면 401")
    void updateMyProfile_invalidToken() throws Exception {
        String requestBody = """
                {
                  "nickname": "newNickname"
                }
                """;

        mockMvc.perform(
                        patch("/users/me")
                                .header("Authorization", "Bearer invalid.jwt.token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("내 정보 수정 - 닉네임 없으면 400")
    void updateMyProfile_validationFail() throws Exception {
        Member member = createMember("test3@test.com", "oldNickname");
        String accessToken = createAccessToken(member);

        String requestBody = """
                {
                  "nickname": ""
                }
                """;

        mockMvc.perform(
                        patch("/users/me")
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}