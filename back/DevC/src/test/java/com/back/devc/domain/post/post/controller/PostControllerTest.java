package com.back.devc.domain.post.post.controller;

import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.repository.MemberRepository;
import com.back.devc.domain.post.category.entity.Category;
import com.back.devc.domain.post.category.repository.CategoryRepository;
import com.back.devc.domain.post.post.dto.PostCreateRequest;
import com.back.devc.domain.post.post.entity.Post;
import com.back.devc.domain.post.post.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class PostControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PostRepository postRepository;

    private Member member;
    private Category category;

    // =========================
    // SETUP
    // =========================
    @BeforeEach
    void setUp() {

        member = memberRepository.save(
                Member.createLocalMember(
                        "test@test.com",
                        "password123!",
                        "testUser"
                )
        );
        // 테스트를 위해, categoryInitData에서 생성된 카테고리 중 첫번째 카테고리 가져옴
        category = categoryRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow();
    }

    // =========================
    // CREATE
    // =========================
    @Test
    @DisplayName("게시글 생성")
    void t1() throws Exception {

        PostCreateRequest request = new PostCreateRequest(
                "테스트글",
                "테스트내용입니다.",
                category.getCategoryId()
        );

        mvc.perform(
                        post("/api/v1/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").exists())
                .andExpect(jsonPath("$.message").value("게시글이 생성되었습니다."));

    }

    // =========================
    // LIST
    // =========================
    @Test
    @DisplayName("게시글 전체 조회")
    void t2() throws Exception {

        Post post = postRepository.save(
                new Post(member, category, "테스트2", "테스트2내용")
        );

        mvc.perform(get("/api/v1/posts"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("테스트2"));
    }

    // =========================
    // DETAIL
    // =========================
    @Test
    @DisplayName("게시글 상세 조회")
    void t3() throws Exception {

        Post post = postRepository.save(
                new Post(member, category, "테스트3", "테스트3내용")
        );

        mvc.perform(get("/api/v1/posts/" + post.getPostId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("테스트3"));
    }

    // =========================
    // UPDATE
    // =========================
    @Test
    @DisplayName("게시글 수정")
    void t4() throws Exception {

        Post post = postRepository.save(
                new Post(member, category, "수정 전 제목", "수정 전 내용")
        );

        mvc.perform(
                        put("/api/v1/posts/" + post.getPostId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                  "title": "수정 후 제목",
                                  "content": "수정 후 내용",
                                  "categoryId": %d
                                }
                                """.formatted(category.getCategoryId()))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정 후 제목"));
    }

    // =========================
    // DELETE
    // =========================
    @Test
    @DisplayName("게시글 삭제")
    void t5() throws Exception {

        Post post = postRepository.save(
                new Post(member, category, "title", "content")
        );

        mvc.perform(delete("/api/v1/posts/" + post.getPostId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("삭제되었습니다."));

        Post deleted = postRepository.findById(post.getPostId()).orElseThrow();
        assertThat(deleted.isDeleted()).isTrue();
        //삭제 후 isDeleted의 값이 true로 변경됨을 보여줌
    }
}