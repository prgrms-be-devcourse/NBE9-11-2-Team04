package com.back.devc.domain.post.comment.attachment.controller;

import com.back.devc.domain.post.comment.attachment.dto.CommentAttachmentDeleteResponse;
import com.back.devc.domain.post.comment.attachment.dto.CommentAttachmentListResponse;
import com.back.devc.domain.post.comment.attachment.service.CommentAttachmentService;
import com.back.devc.global.security.jwt.JwtProvider;
import com.back.devc.domain.member.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(CommentAttachmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class CommentAttachmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentAttachmentService commentAttachmentService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private MemberRepository memberRepository;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("댓글 첨부 업로드 API 호출 성공")
    void uploadCommentAttachments_success() throws Exception {
        CommentAttachmentListResponse response = org.mockito.Mockito.mock(CommentAttachmentListResponse.class);

        given(commentAttachmentService.uploadAttachments(eq(1L), anyList(), anyList()))
                .willReturn(response);

        MockMultipartFile file = new MockMultipartFile(
                "files",
                "test.jpg",
                "image/jpeg",
                "dummy-image".getBytes()
        );

        mockMvc.perform(multipart("/api/comments/{commentId}/attachments", 1L)
                        .file(file)
                        .param("fileOrder", "1"))
                .andExpect(status().isOk());

        verify(commentAttachmentService).uploadAttachments(eq(1L), anyList(), anyList());
    }

    @Test
    @DisplayName("댓글 첨부 목록 조회 API 호출 성공")
    void getCommentAttachments_success() throws Exception {
        CommentAttachmentListResponse response = org.mockito.Mockito.mock(CommentAttachmentListResponse.class);

        given(commentAttachmentService.getAttachments(1L)).willReturn(response);

        mockMvc.perform(get("/api/comments/{commentId}/attachments", 1L))
                .andExpect(status().isOk());

        verify(commentAttachmentService).getAttachments(1L);
    }

    @Test
    @DisplayName("댓글 첨부 삭제 API 호출 성공")
    void deleteCommentAttachment_success() throws Exception {
        CommentAttachmentDeleteResponse response = org.mockito.Mockito.mock(CommentAttachmentDeleteResponse.class);

        given(commentAttachmentService.deleteAttachment(1L, 1L)).willReturn(response);

        mockMvc.perform(delete("/api/comments/{commentId}/attachments/{attachmentId}", 1L, 1L))
                .andExpect(status().isOk());

        verify(commentAttachmentService).deleteAttachment(1L, 1L);
    }
}