package com.back.devc.domain.post.comment.attachment.service;

import com.back.devc.domain.post.comment.attachment.dto.CommentAttachmentListResponse;
import com.back.devc.domain.post.comment.attachment.entity.CommentAttachment;
import com.back.devc.domain.post.comment.attachment.repository.CommentAttachmentRepository;
import com.back.devc.domain.post.comment.entity.Comment;
import com.back.devc.domain.post.comment.repository.CommentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentAttachmentServiceImplTest {

    @Mock
    private CommentAttachmentRepository commentAttachmentRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentAttachmentServiceImpl commentAttachmentService;

    @Test
    @DisplayName("댓글 첨부 목록 조회 성공")
    void getAttachments_success() {
        Comment comment = mock(Comment.class);
        given(comment.isDeleted()).willReturn(false);
        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        CommentAttachment attachment1 = CommentAttachment.create(
                1L,
                "test1.jpg",
                "uuid_test1.jpg",
                "/uploads/comments/uuid_test1.jpg",
                "IMAGE",
                "image/jpeg",
                123L,
                1
        );

        CommentAttachment attachment2 = CommentAttachment.create(
                1L,
                "test2.pdf",
                "uuid_test2.pdf",
                "/uploads/comments/uuid_test2.pdf",
                "FILE",
                "application/pdf",
                456L,
                2
        );

        given(commentAttachmentRepository.findByCommentIdOrderByFileOrderAscIdAsc(1L))
                .willReturn(List.of(attachment1, attachment2));

        CommentAttachmentListResponse response = commentAttachmentService.getAttachments(1L);

        assertThat(response).isNotNull();
        assertThat(response.getAttachments()).hasSize(2);
        assertThat(response.getAttachments().get(0).getCommentId()).isEqualTo(1L);
        assertThat(response.getAttachments().get(0).getFileName()).isEqualTo("test1.jpg");
        assertThat(response.getAttachments().get(1).getFileName()).isEqualTo("test2.pdf");

        verify(commentRepository).findById(1L);
        verify(commentAttachmentRepository).findByCommentIdOrderByFileOrderAscIdAsc(1L);
    }

    @Test
    @DisplayName("존재하지 않는 댓글이면 첨부 목록 조회 시 예외 발생")
    void getAttachments_fail_whenCommentNotFound() {
        given(commentRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> commentAttachmentService.getAttachments(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("댓글을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("삭제된 댓글이면 첨부 목록 조회 시 예외 발생")
    void getAttachments_fail_whenCommentDeleted() {
        Comment deletedComment = mock(Comment.class);
        given(deletedComment.isDeleted()).willReturn(true);
        given(commentRepository.findById(1L)).willReturn(Optional.of(deletedComment));

        assertThatThrownBy(() -> commentAttachmentService.getAttachments(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("삭제된 댓글에는 첨부파일을 처리할 수 없습니다.");
    }
}
