package com.back.devc;

import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.repository.MemberRepository;
import com.back.devc.domain.post.category.entity.Category;
import com.back.devc.domain.post.category.repository.CategoryRepository;
import com.back.devc.domain.post.post.entity.Post;
import com.back.devc.domain.post.post.repository.PostRepository;
import com.back.devc.global.security.jwt.JwtProvider;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private JwtProvider jwtProvider;

    private Member createMember() {
        return memberRepository.save(
                Member.createLocalMember("test@test.com", "dummy-password", "tester")
        );
    }

    private Category createCategory() {
        return categoryRepository.save(new Category("자유게시판"));
    }

    private Post createPost(Member member, Category category) {
        return postRepository.save(new Post(member, category, "테스트 제목", "테스트 내용"));
    }

    private String createAccessToken(Member member) {
        return jwtProvider.createAccessToken(member);
    }

    @Test
    @DisplayName("좋아요 등록 성공")
    void createLike_success() throws Exception {
        Member member = createMember();
        Category category = createCategory();
        Post post = createPost(member, category);
        String accessToken = createAccessToken(member);

        mockMvc.perform(
                        post("/api/posts/{postId}/likes", post.getPostId())
                                .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(post.getPostId()))
                .andExpect(jsonPath("$.liked").value(true));
    }

    @Test
    void deleteLike_success() throws Exception {
        Member member = createMember();
        Category category = createCategory();
        Post post = createPost(member, category);
        String accessToken = createAccessToken(member);

        mockMvc.perform(post("/api/posts/{postId}/likes", post.getPostId())
                .header("Authorization", "Bearer " + accessToken));

        mockMvc.perform(
                        delete("/api/posts/{postId}/likes", post.getPostId())
                                .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(false));
    }

    @Test
    void createBookmark_success() throws Exception {
        Member member = createMember();
        Category category = createCategory();
        Post post = createPost(member, category);
        String accessToken = createAccessToken(member);

        mockMvc.perform(
                        post("/api/posts/{postId}/bookmarks", post.getPostId())
                                .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookmarked").value(true));
    }

    @Test
    void deleteBookmark_success() throws Exception {
        Member member = createMember();
        Category category = createCategory();
        Post post = createPost(member, category);
        String accessToken = createAccessToken(member);

        mockMvc.perform(post("/api/posts/{postId}/bookmarks", post.getPostId())
                .header("Authorization", "Bearer " + accessToken));

        mockMvc.perform(
                        delete("/api/posts/{postId}/bookmarks", post.getPostId())
                                .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookmarked").value(false));
    }

    @Test
    void searchLog_create_and_get_success() throws Exception {
        Member member = createMember();
        String accessToken = createAccessToken(member);

        mockMvc.perform(
                        post("/api/search-logs")
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "keyword": "spring boot"
                                        }
                                        """)
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/api/users/me/search-logs")
                                .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void mypage_profile_success() throws Exception {
        Member member = createMember();
        String accessToken = createAccessToken(member);

        mockMvc.perform(
                        get("/api/mypage")
                                .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(member.getUserId()))
                .andExpect(jsonPath("$.nickname").value("tester"));
    }

    @Test
    void mypage_posts_success() throws Exception {
        Member member = createMember();
        Category category = createCategory();
        Post post = createPost(member, category);
        String accessToken = createAccessToken(member);

        mockMvc.perform(
                        get("/api/mypage/posts")
                                .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void mypage_likes_success() throws Exception {
        Member member = createMember();
        Category category = createCategory();
        Post post = createPost(member, category);
        String accessToken = createAccessToken(member);

        mockMvc.perform(
                post("/api/posts/{postId}/likes", post.getPostId())
                        .header("Authorization", "Bearer " + accessToken)
        );

        mockMvc.perform(
                        get("/api/users/me/likes")
                                .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void mypage_bookmarks_success() throws Exception {
        Member member = createMember();
        Category category = createCategory();
        Post post = createPost(member, category);
        String accessToken = createAccessToken(member);

        mockMvc.perform(
                post("/api/posts/{postId}/bookmarks", post.getPostId())
                        .header("Authorization", "Bearer " + accessToken)
        );

        mockMvc.perform(
                        get("/api/users/me/bookmarks")
                                .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void updateMyProfile_success() throws Exception {
        Member member = createMember();
        String accessToken = createAccessToken(member);

        mockMvc.perform(
                        patch("/api/mypage")
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "nickname": "newTester"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("newTester"));
    }
}