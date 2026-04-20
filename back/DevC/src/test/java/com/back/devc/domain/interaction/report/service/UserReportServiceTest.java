package com.back.devc.domain.interaction.report.service;

import com.back.devc.domain.interaction.report.dto.ReportRequestDTO;
import com.back.devc.domain.interaction.report.repository.ReportRepository;
import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.repository.MemberRepository;
import com.back.devc.domain.interaction.notification.service.NotificationService;
import com.back.devc.domain.post.comment.entity.Comment;
import com.back.devc.domain.post.comment.repository.CommentRepository;
import com.back.devc.domain.post.post.entity.Post;
import com.back.devc.domain.post.post.repository.PostRepository;
import com.back.devc.global.exception.ApiException;
import com.back.devc.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.mockito.BDDMockito.lenient;
import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserReportService 단위 테스트")
class UserReportServiceTest {

    @InjectMocks
    private UserReportService userReportService;

    @Mock private ReportRepository reportRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private PostRepository postRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private NotificationService notificationService;

    private Member reporter;
    private Member postAuthor;
    private Post activePost;
    private Post deletedPost;
    private Comment activeComment;
    private Comment deletedComment;
    private ReportRequestDTO dto;

    @BeforeEach
    void setUp() {
        reporter   = mock(Member.class);
        postAuthor = mock(Member.class);

        // lenient().when() 형식을 사용하면 BDDMockito 충돌 없이 작동합니다.
        lenient().when(reporter.getUserId()).thenReturn(1L);
        lenient().when(postAuthor.getUserId()).thenReturn(2L);

        // 정상 게시글
        activePost = mock(Post.class);
        lenient().when(activePost.getMember()).thenReturn(postAuthor);
        lenient().when(activePost.isDeleted()).thenReturn(false);

        // 삭제된 게시글
        deletedPost = mock(Post.class);
        lenient().when(deletedPost.getMember()).thenReturn(postAuthor);
        lenient().when(deletedPost.isDeleted()).thenReturn(true);

        // 정상 댓글
        activeComment = mock(Comment.class);
        lenient().when(activeComment.getUserId()).thenReturn(2L);
        lenient().when(activeComment.isDeleted()).thenReturn(false);

        // 삭제된 댓글
        deletedComment = mock(Comment.class);
        lenient().when(deletedComment.getUserId()).thenReturn(2L);
        lenient().when(deletedComment.isDeleted()).thenReturn(true);

        // DTO 초기화
        dto = new ReportRequestDTO();
        dto.setTargetId(10L);
        dto.setReasonType("ABUSE");
        dto.setReasonDetail("욕설이 포함되어 있습니다.");
    }

    // ════════════════════════════════════════════════════════════
    // 게시글 신고 (reportPost)
    // ════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("게시글 신고")
    class ReportPostTest {

        @Test
        @DisplayName("[성공] 정상적인 게시글 신고 - Report 엔티티가 저장된다")
        void reportPost_success() {
            // given
            given(memberRepository.findById(1L)).willReturn(Optional.of(reporter));
            given(postRepository.findById(10L)).willReturn(Optional.of(activePost));
            given(reportRepository.existsByReporterAndTargetTypeAndTargetId(reporter, "POST", 10L))
                    .willReturn(false);

            // when
            userReportService.reportPost(1L, dto);

            // then
            verify(reportRepository, times(1)).save(argThat(report ->
                    "POST".equals(report.getTargetType()) &&
                            report.getTargetId().equals(10L) &&
                            "PENDING".equals(report.getStatus()) &&
                            "ABUSE".equals(report.getReasonType())
            ));
            verify(notificationService, times(1)).createPostReportNotification(10L, 1L);
        }

        @Test
        @DisplayName("[실패] 신고자가 존재하지 않으면 MEMBER_NOT_FOUND 예외")
        void reportPost_memberNotFound() {
            given(memberRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userReportService.reportPost(1L, dto))
                    .isInstanceOf(ApiException.class)
                    .extracting(e -> ((ApiException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);

            verify(reportRepository, never()).save(any());
        }

        @Test
        @DisplayName("[실패] 게시글이 존재하지 않으면 POST_NOT_FOUND 예외")
        void reportPost_postNotFound() {
            given(memberRepository.findById(1L)).willReturn(Optional.of(reporter));
            given(postRepository.findById(10L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userReportService.reportPost(1L, dto))
                    .isInstanceOf(ApiException.class)
                    .extracting(e -> ((ApiException) e).getErrorCode())
                    .isEqualTo(ErrorCode.POST_NOT_FOUND);

            verify(reportRepository, never()).save(any());
        }

        @Test
        @DisplayName("[실패] 본인 게시글 신고 시 CANNOT_REPORT_SELF 예외")
        void reportPost_cannotReportSelf() {
            // reporter(1L)가 작성한 게시글을 reporter(1L)가 신고 시도
            given(activePost.getMember()).willReturn(reporter); // 작성자 = 신고자

            given(memberRepository.findById(1L)).willReturn(Optional.of(reporter));
            given(postRepository.findById(10L)).willReturn(Optional.of(activePost));

            assertThatThrownBy(() -> userReportService.reportPost(1L, dto))
                    .isInstanceOf(ApiException.class)
                    .extracting(e -> ((ApiException) e).getErrorCode())
                    .isEqualTo(ErrorCode.CANNOT_REPORT_SELF);

            verify(reportRepository, never()).save(any());
        }

        @Test
        @DisplayName("[실패] 이미 삭제된 게시글 신고 시 ALREADY_DELETED 예외")
        void reportPost_alreadyDeleted() {
            given(memberRepository.findById(1L)).willReturn(Optional.of(reporter));
            given(postRepository.findById(10L)).willReturn(Optional.of(deletedPost));

            assertThatThrownBy(() -> userReportService.reportPost(1L, dto))
                    .isInstanceOf(ApiException.class)
                    .extracting(e -> ((ApiException) e).getErrorCode())
                    .isEqualTo(ErrorCode.ALREADY_DELETED);

            verify(reportRepository, never()).save(any());
        }

        @Test
        @DisplayName("[실패] 동일 게시글 중복 신고 시 REPORT_ALREADY_EXISTS 예외")
        void reportPost_duplicateReport() {
            given(memberRepository.findById(1L)).willReturn(Optional.of(reporter));
            given(postRepository.findById(10L)).willReturn(Optional.of(activePost));
            given(reportRepository.existsByReporterAndTargetTypeAndTargetId(reporter, "POST", 10L))
                    .willReturn(true);

            assertThatThrownBy(() -> userReportService.reportPost(1L, dto))
                    .isInstanceOf(ApiException.class)
                    .extracting(e -> ((ApiException) e).getErrorCode())
                    .isEqualTo(ErrorCode.REPORT_ALREADY_EXISTS);

            verify(reportRepository, never()).save(any());
        }
    }

    // ════════════════════════════════════════════════════════════
    // 댓글 신고 (reportComment)
    // ════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("댓글 신고")
    class ReportCommentTest {

        @BeforeEach
        void commentSetUp() {
            dto.setTargetId(20L);
        }

        @Test
        @DisplayName("[성공] 정상적인 댓글 신고 - Report 엔티티가 저장된다")
        void reportComment_success() {
            given(memberRepository.findById(1L)).willReturn(Optional.of(reporter));
            given(commentRepository.findById(20L)).willReturn(Optional.of(activeComment));
            given(reportRepository.existsByReporterAndTargetTypeAndTargetId(reporter, "COMMENT", 20L))
                    .willReturn(false);

            userReportService.reportComment(1L, dto);

            verify(reportRepository, times(1)).save(argThat(report ->
                    "COMMENT".equals(report.getTargetType()) &&
                            report.getTargetId().equals(20L) &&
                            "PENDING".equals(report.getStatus())
            ));
            verify(notificationService, times(1)).createCommentReportNotification(20L, 1L);
        }

        @Test
        @DisplayName("[실패] 신고자가 존재하지 않으면 MEMBER_NOT_FOUND 예외")
        void reportComment_memberNotFound() {
            given(memberRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userReportService.reportComment(1L, dto))
                    .isInstanceOf(ApiException.class)
                    .extracting(e -> ((ApiException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);

            verify(reportRepository, never()).save(any());
        }

        @Test
        @DisplayName("[실패] 댓글이 존재하지 않으면 COMMENT_NOT_FOUND 예외")
        void reportComment_commentNotFound() {
            given(memberRepository.findById(1L)).willReturn(Optional.of(reporter));
            given(commentRepository.findById(20L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userReportService.reportComment(1L, dto))
                    .isInstanceOf(ApiException.class)
                    .extracting(e -> ((ApiException) e).getErrorCode())
                    .isEqualTo(ErrorCode.COMMENT_NOT_FOUND);

            verify(reportRepository, never()).save(any());
        }

        @Test
        @DisplayName("[실패] 본인 댓글 신고 시 CANNOT_REPORT_SELF 예외")
        void reportComment_cannotReportSelf() {
            // 댓글 작성자 userId = 신고자 userId (1L)
            given(activeComment.getUserId()).willReturn(1L);

            given(memberRepository.findById(1L)).willReturn(Optional.of(reporter));
            given(commentRepository.findById(20L)).willReturn(Optional.of(activeComment));

            assertThatThrownBy(() -> userReportService.reportComment(1L, dto))
                    .isInstanceOf(ApiException.class)
                    .extracting(e -> ((ApiException) e).getErrorCode())
                    .isEqualTo(ErrorCode.CANNOT_REPORT_SELF);

            verify(reportRepository, never()).save(any());
        }

        @Test
        @DisplayName("[실패] 이미 삭제된 댓글 신고 시 ALREADY_DELETED 예외")
        void reportComment_alreadyDeleted() {
            given(memberRepository.findById(1L)).willReturn(Optional.of(reporter));
            given(commentRepository.findById(20L)).willReturn(Optional.of(deletedComment));

            assertThatThrownBy(() -> userReportService.reportComment(1L, dto))
                    .isInstanceOf(ApiException.class)
                    .extracting(e -> ((ApiException) e).getErrorCode())
                    .isEqualTo(ErrorCode.ALREADY_DELETED);

            verify(reportRepository, never()).save(any());
        }

        @Test
        @DisplayName("[실패] 동일 댓글 중복 신고 시 REPORT_ALREADY_EXISTS 예외")
        void reportComment_duplicateReport() {
            given(memberRepository.findById(1L)).willReturn(Optional.of(reporter));
            given(commentRepository.findById(20L)).willReturn(Optional.of(activeComment));
            given(reportRepository.existsByReporterAndTargetTypeAndTargetId(reporter, "COMMENT", 20L))
                    .willReturn(true);

            assertThatThrownBy(() -> userReportService.reportComment(1L, dto))
                    .isInstanceOf(ApiException.class)
                    .extracting(e -> ((ApiException) e).getErrorCode())
                    .isEqualTo(ErrorCode.REPORT_ALREADY_EXISTS);

            verify(reportRepository, never()).save(any());
        }
    }
}