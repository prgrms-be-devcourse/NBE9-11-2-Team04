package com.back.devc;

import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.repository.MemberRepository;
import com.back.devc.domain.post.category.entity.Category;
import com.back.devc.domain.post.category.repository.CategoryRepository;
import com.back.devc.domain.post.post.entity.Post;
import com.back.devc.domain.post.post.repository.PostRepository;
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
@AutoConfigureMockMvc(addFilters = false)
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

    @Test
    @DisplayName("좋아요 등록 성공")
    void createLike_success() throws Exception {
        Member member = createMember();
        Category category = createCategory();
        Post post = createPost(member, category);

        mockMvc.perform(post("/posts/{postId}/likes", post.getPostId())
                        .param("userId", member.getUserId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(post.getPostId()))
                .andExpect(jsonPath("$.liked").value(true));
    }

    @Test
    @DisplayName("좋아요 취소 성공")
    void deleteLike_success() throws Exception {
        Member member = createMember();
        Category category = createCategory();
        Post post = createPost(member, category);

        mockMvc.perform(post("/posts/{postId}/likes", post.getPostId())
                        .param("userId", member.getUserId().toString()))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/posts/{postId}/likes", post.getPostId())
                        .param("userId", member.getUserId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(post.getPostId()))
                .andExpect(jsonPath("$.liked").value(false));
    }

    @Test
    @DisplayName("북마크 등록 성공")
    void createBookmark_success() throws Exception {
        Member member = createMember();
        Category category = createCategory();
        Post post = createPost(member, category);

        mockMvc.perform(post("/posts/{postId}/bookmarks", post.getPostId())
                        .param("userId", member.getUserId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(post.getPostId()))
                .andExpect(jsonPath("$.bookmarked").value(true));
    }

    @Test
    @DisplayName("북마크 취소 성공")
    void deleteBookmark_success() throws Exception {
        Member member = createMember();
        Category category = createCategory();
        Post post = createPost(member, category);

        mockMvc.perform(post("/posts/{postId}/bookmarks", post.getPostId())
                        .param("userId", member.getUserId().toString()))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/posts/{postId}/bookmarks", post.getPostId())
                        .param("userId", member.getUserId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(post.getPostId()))
                .andExpect(jsonPath("$.bookmarked").value(false));
    }

    @Test
    @DisplayName("검색기록 저장 및 조회 성공")
    void searchLog_create_and_get_success() throws Exception {
        Member member = createMember();

        mockMvc.perform(post("/search-logs")
                        .param("userId", member.getUserId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "keyword": "spring boot"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/me/search-logs")
                        .param("userId", member.getUserId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("마이페이지 내 정보 조회 성공")
    void mypage_profile_success() throws Exception {
        Member member = createMember();

        mockMvc.perform(get("/users/me")
                        .param("userId", member.getUserId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(member.getUserId()))
                .andExpect(jsonPath("$.nickname").value("tester"))
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    @DisplayName("마이페이지 내가 쓴 글 조회 성공")
    void mypage_posts_success() throws Exception {
        Member member = createMember();
        Category category = createCategory();
        Post post = createPost(member, category);

        mockMvc.perform(get("/users/me/posts")
                        .param("userId", member.getUserId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].postId").value(post.getPostId()))
                .andExpect(jsonPath("$[0].title").value("테스트 제목"));
    }

    @Test
    @DisplayName("마이페이지 좋아요한 글 조회 성공")
    void mypage_likes_success() throws Exception {
        Member member = createMember();
        Category category = createCategory();
        Post post = createPost(member, category);

        mockMvc.perform(post("/posts/{postId}/likes", post.getPostId())
                        .param("userId", member.getUserId().toString()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/me/likes")
                        .param("userId", member.getUserId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("마이페이지 북마크한 글 조회 성공")
    void mypage_bookmarks_success() throws Exception {
        Member member = createMember();
        Category category = createCategory();
        Post post = createPost(member, category);

        mockMvc.perform(post("/posts/{postId}/bookmarks", post.getPostId())
                        .param("userId", member.getUserId().toString()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/me/bookmarks")
                        .param("userId", member.getUserId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}