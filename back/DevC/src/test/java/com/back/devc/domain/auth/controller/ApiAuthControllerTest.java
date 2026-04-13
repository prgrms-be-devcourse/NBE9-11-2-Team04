package com.back.devc.domain.auth.controller;

import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class ApiAuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void 회원가입() throws Exception {
        String email = "new-user@test.com";
        String password = "password123!";
        String nickname = "newUser";

        ResultActions resultActions = mvc
                .perform(
                        post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "%s",
                                          "password": "%s",
                                          "nickname": "%s"
                                        }
                                        """.formatted(email, password, nickname))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(AuthController.class))
                .andExpect(handler().methodName("signUp"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("AUTH_201_SIGNUP_SUCCESS"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.userId").isNumber())
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.nickname").value(nickname))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        Member savedMember = memberRepository.findByEmail(email).orElseThrow();
        assertThat(savedMember.getNickname()).isEqualTo(nickname);
        assertThat(savedMember.getPasswordHash()).isNotEqualTo(password);
        assertThat(passwordEncoder.matches(password, savedMember.getPasswordHash())).isTrue();
    }

    @Test
    void 로그인() throws Exception {
        String email = "login-user@test.com";
        String rawPassword = "password123!";
        String nickname = "loginUser";

        Member member = Member.createLocalMember(email, passwordEncoder.encode(rawPassword), nickname);
        memberRepository.save(member);

        ResultActions resultActions = mvc
                .perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "%s",
                                          "password": "%s"
                                        }
                                        """.formatted(email, rawPassword))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(AuthController.class))
                .andExpect(handler().methodName("login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("AUTH_200_LOGIN_SUCCESS"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.userId").isNumber())
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.nickname").value(nickname))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }
}
