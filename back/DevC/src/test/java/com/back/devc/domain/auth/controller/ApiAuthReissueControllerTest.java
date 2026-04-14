package com.back.devc.domain.auth.controller;

import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.repository.MemberRepository;
import com.jayway.jsonpath.JsonPath;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class ApiAuthReissueControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${custom.jwt.secret-key}")
    private String jwtSecretKey;

    @Test
    void 리프레시토큰으로_재발급_성공() throws Exception {
        String email = "reissue-user@test.com";
        String rawPassword = "password123!";
        String nickname = "reissueUser";

        Member member = Member.createLocalMember(email, passwordEncoder.encode(rawPassword), nickname);
        memberRepository.save(member);

        String loginResponse = mvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "%s",
                                          "password": "%s"
                                        }
                                        """.formatted(email, rawPassword))
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshToken = JsonPath.read(loginResponse, "$.data.refreshToken");

        ResultActions resultActions = mvc.perform(
                        post("/api/auth/reissue")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "refreshToken": "%s"
                                        }
                                        """.formatted(refreshToken))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(AuthController.class))
                .andExpect(handler().methodName("reissue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("AUTH_200_REISSUE_SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    void 액세스토큰으로_재발급요청_실패() throws Exception {
        String email = "reissue-fail@test.com";
        String rawPassword = "password123!";
        String nickname = "reissueFailUser";

        Member member = Member.createLocalMember(email, passwordEncoder.encode(rawPassword), nickname);
        memberRepository.save(member);

        String loginResponse = mvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "%s",
                                          "password": "%s"
                                        }
                                        """.formatted(email, rawPassword))
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accessToken = JsonPath.read(loginResponse, "$.data.accessToken");

        ResultActions resultActions = mvc.perform(
                        post("/api/auth/reissue")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "refreshToken": "%s"
                                        }
                                        """.formatted(accessToken))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(AuthController.class))
                .andExpect(handler().methodName("reissue"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_401_INVALID_TOKEN_TYPE"));
    }

    @Test
    void 만료된_리프레시토큰으로_재발급요청_실패() throws Exception {
        String email = "reissue-expired@test.com";
        String rawPassword = "password123!";
        String nickname = "reissueExpiredUser";

        Member member = Member.createLocalMember(email, passwordEncoder.encode(rawPassword), nickname);
        Member savedMember = memberRepository.save(member);

        String expiredRefreshToken = Jwts.builder()
                .subject(String.valueOf(savedMember.getUserId()))
                .claim("tokenType", "REFRESH")
                .issuedAt(Date.from(Instant.now().minusSeconds(120)))
                .expiration(Date.from(Instant.now().minusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)))
                .compact();

        ResultActions resultActions = mvc.perform(
                        post("/api/auth/reissue")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "refreshToken": "%s"
                                        }
                                        """.formatted(expiredRefreshToken))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(AuthController.class))
                .andExpect(handler().methodName("reissue"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_401_EXPIRED_TOKEN"));
    }
}
