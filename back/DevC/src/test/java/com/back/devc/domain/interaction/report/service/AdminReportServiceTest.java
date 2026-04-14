package com.back.devc.domain.interaction.report.service;

import com.back.devc.domain.interaction.report.dto.AdminReportRequestDTO;
import com.back.devc.domain.interaction.report.dto.ReportResponseDTO;
import com.back.devc.domain.interaction.report.entity.Report;
import com.back.devc.domain.interaction.report.repository.ReportRepository;
import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.entity.MemberStatus;
import com.back.devc.domain.member.member.repository.MemberRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminReportService 테스트")
class AdminReportServiceTest {

    @InjectMocks
    private AdminReportService adminReportService;

    @Mock private ReportRepository reportRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private PostRepository postRepository;
    @Mock private CommentRepository commentRepository;

    // ── 공통 픽스처 ──────────────────────────────────────────────
    private Member admin;
    private Member postAuthor;
    private AdminReportRequestDTO dto;

    @BeforeEach
    void setUp() {
        admin      = mock(Member.class);
        postAuthor = mock(Member.class);

        given(admin.getUserId()).willReturn(99L);
        given(postAuthor.getUserId()).willReturn(2L);

        dto = new AdminReportRequestDTO();
        dto.setReportId(1L);
    }

    // ── 신고 Report 목 생성 헬퍼 ─────────────────────────────────
    private Report pendingPostReport() {
        Report r = mock(Report.class);
        given(r.getStatus()).willReturn("PENDING");
        given(r.getTargetType()).willReturn("POST");
        given(r.getTargetId()).willReturn(10L);
        return r;
    }

    private Report pendingCommentReport() {
        Report r = mock(Report.class);
        given(r.getStatus()).willReturn("PENDING");
        given(r.getTargetType()).willReturn("COMMENT");
        given(r.getTargetId()).willReturn(20L);
        return r;
    }

    private Report resolvedReport() {
        Report r = mock(Report.class);
        given(r.getStatus()).willReturn("RESOLVED");
        return r;
    }

    private Report rejectedReport() {
        Report r = mock(Report.class);
        given(r.getStatus()).willReturn("REJECTED");
        return r;
    }

    // ════════════════════════════════════════════════════════════
    // 대기 신고 목록 조회 (getPendingReports)
    // ════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("대기 신고 목록 조회")
    class GetPendingReportsTest {

        @Test
        @DisplayName("[성공] PENDING 상태 신고가 있으면 DTO 리스트를 반환한다")
        void getPendingReports_returnsList() {
            // given - Report.from()이 호출될 수 있도록 실제 Report를 빌더로 생성
            Member mockReporter = mock(Member.class);
            given(mockReporter.getUserId()).willReturn(1L);
            given(mockReporter.getNickname()).willReturn("tester");

            Report report = Report.builder()
                    .reporter(mockReporter)
                    .targetType("POST")
                    .targetId(10L)
                    .reasonType("ABUSE")
                    .reasonDetail("욕설")
                    .build();

            given(reportRepository.findAllByStatus("PENDING")).willReturn(List.of(report));

            // when
            List<ReportResponseDTO> result = adminReportService.getPendingReports();

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTargetType()).isEqualTo("POST");
            assertThat(result.get(0).getStatus()).isEqualTo("PENDING");
        }

        @Test
        @DisplayName("[성공] PENDING 신고가 없으면 빈 리스트를 반환한다")
        void getPendingReports_emptyList() {
            given(reportRepository.findAllByStatus("PENDING")).willReturn(List.of());

            List<ReportResponseDTO> result = adminReportService.getPendingReports();

            assertThat(result).isEmpty();
        }
    }

    // ════════════════════════════════════════════════════════════
    // 신고 승인 - POST (approveReport / POST)
    // ════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("신고 승인 - 게시글")
    class ApprovePostReportTest {

        private Post post;

        @BeforeEach
        void postSetUp() {
            post = mock(Post.class);
            given(post.getMember()).willReturn(postAuthor);
        }

        @Test
        @DisplayName("[성공] POST 신고 승인 → 게시글 soft delete, 작성자 ACTIVE→WARNED 제재")
        void approvePostReport_activeAuthor_becomesWarned() {
            Report report = pendingPostReport();
            given(postAuthor.getStatus()).willReturn(MemberStatus.ACTIVE);

            given(reportRepository.findById(1L)).willReturn(Optional.of(report));
            given(memberRepository.findById(99L)).willReturn(Optional.of(admin));
            given(postRepository.findById(10L)).willReturn(Optional.of(post));

            adminReportService.approveReport(99L, dto);

            verify(report).processReport(admin, "RESOLVED");
            verify(post).delete();
            verify(postAuthor).updateStatus(MemberStatus.WARNED);
        }

        @Test
        @DisplayName("[성공] POST 신고 승인 → 작성자가 WARNED이면 BLACKLISTED로 제재")
        void approvePostReport_warnedAuthor_becomesBlacklisted() {
            Report report = pendingPostReport();
            given(postAuthor.getStatus()).willReturn(MemberStatus.WARNED);

            given(reportRepository.findById(1L)).willReturn(Optional.of(report));
            given(memberRepository.findById(99L)).willReturn(Optional.of(admin));
            given(postRepository.findById(10L)).willReturn(Optional.of(post));

            adminReportService.approveReport(99L, dto);

            verify(post).delete();
            verify(postAuthor).updateStatus(MemberStatus.BLACKLISTED);
        }

        @Test
        @DisplayName("[성공] POST 신고 승인 → 작성자가 이미 BLACKLISTED이면 상태 변경 없음")
        void approvePostReport_blacklistedAuthor_noChange() {
            Report report = pendingPostReport();
            given(postAuthor.getStatus()).willReturn(MemberStatus.BLACKLISTED);

            given(reportRepository.findById(1L)).willReturn(Optional.of(report));
            given(memberRepository.findById(99L)).willReturn(Optional.of(admin));
            given(postRepository.findById(10L)).willReturn(Optional.of(post));

            adminReportService.approveReport(99L, dto);

            verify(post).delete();
            // BLACKLISTED 이상의 제재는 없으므로 updateStatus 가 호출되지 않아야 함
            verify(postAuthor, never()).updateStatus(any());
        }

        @Test
        @DisplayName("[실패] 신고 대상 게시글이 없으면 POST_NOT_FOUND 예외")
        void approvePostReport_postNotFound() {
            Report report = pendingPostReport();

            given(reportRepository.findById(1L)).willReturn(Optional.of(report));
            given(memberRepository.findById(99L)).willReturn(Optional.of(admin));
            given(postRepository.findById(10L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> adminReportService.approveReport(99L, dto))
                    .isInstanceOf(ApiException.class)
                    .extracting(e -> ((ApiException) e).getErrorCode())
                    .isEqualTo(ErrorCode.POST_NOT_FOUND);

            verify(post, never()).delete();
        }
    }

    // ════════════════════════════════════════════════════════════
    // 신고 승인 - COMMENT (approveReport / COMMENT)
    // ════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("신고 승인 - 댓글")
    class ApproveCommentReportTest {

        private Comment comment;

        @BeforeEach
        void commentSetUp() {
            comment = mock(Comment.class);
            given(comment.getUserId()).willReturn(2L);
        }

        @Test
        @DisplayName("[성공] COMMENT 신고 승인 → 댓글 soft delete, 작성자 ACTIVE→WARNED 제재")
        void approveCommentReport_activeAuthor_becomesWarned() {
            Report report = pendingCommentReport();
            given(postAuthor.getStatus()).willReturn(MemberStatus.ACTIVE);

            given(reportRepository.findById(1L)).willReturn(Optional.of(report));
            given(memberRepository.findById(99L)).willReturn(Optional.of(admin));
            given(commentRepository.findById(20L)).willReturn(Optional.of(comment));
            given(memberRepository.findById(2L)).willReturn(Optional.of(postAuthor));

            adminReportService.approveReport(99L, dto);

            verify(report).processReport(admin, "RESOLVED");
            verify(comment).softDelete();
            verify(postAuthor).updateStatus(MemberStatus.WARNED);
        }

        @Test
        @DisplayName("[성공] COMMENT 신고 승인 → 작성자가 WARNED이면 BLACKLISTED로 제재")
        void approveCommentReport_warnedAuthor_becomesBlacklisted() {
            Report report = pendingCommentReport();
            given(postAuthor.getStatus()).willReturn(MemberStatus.WARNED);

            given(reportRepository.findById(1L)).willReturn(Optional.of(report));
            given(memberRepository.findById(99L)).willReturn(Optional.of(admin));
            given(commentRepository.findById(20L)).willReturn(Optional.of(comment));
            given(memberRepository.findById(2L)).willReturn(Optional.of(postAuthor));

            adminReportService.approveReport(99L, dto);

            verify(comment).softDelete();
            verify(postAuthor).updateStatus(MemberStatus.BLACKLISTED);
        }

        @Test
        @DisplayName("[실패] 신고 대상 댓글이 없으면 COMMENT_NOT_FOUND 예외")
        void approveCommentReport_commentNotFound() {
            Report report = pendingCommentReport();

            given(reportRepository.findById(1L)).willReturn(Optional.of(report));
            given(memberRepository.findById(99L)).willReturn(Optional.of(admin));
            given(commentRepository.findById(20L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> adminReportService.approveReport(99L, dto))
                    .isInstanceOf(ApiException.class)
                    .extracting(e -> ((ApiException) e).getErrorCode())
                    .isEqualTo(ErrorCode.COMMENT_NOT_FOUND);

            verify(comment, never()).softDelete();
        }
    }

    // ════════════════════════════════════════════════════════════
    // 신고 승인 - 공통 예외 케이스
    // ════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("신고 승인 - 공통 예외")
    class ApproveReportCommonExceptionTest {

        @Test
        @DisplayName("[실패] 신고가 존재하지 않으면 REPORT_NOT_FOUND 예외")
        void approveReport_reportNotFound() {
            given(reportRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> adminReportService.approveReport(99L, dto))
                    .isInstanceOf(ApiException.class)
                    .extracting(e -> ((ApiException) e).getErrorCode())
                    .isEqualTo(ErrorCode.REPORT_NOT_FOUND);
        }

        @Test
        @DisplayName("[실패] 관리자가 존재하지 않으면 MEMBER_NOT_FOUND 예외")
        void approveReport_adminNotFound() {
            given(reportRepository.findById(1L)).willReturn(Optional.of(pendingPostReport()));
            given(memberRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> adminReportService.approveReport(99L, dto))
                    .isInstanceOf(ApiException.class)
                    .extracting(e -> ((ApiException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
        }

        @Test
        @DisplayName("[실패] 이미 RESOLVED된 신고를 승인하면 REPORT_ALREADY_PROCESSED 예외")
        void approveReport_alreadyResolved() {
            given(reportRepository.findById(1L)).willReturn(Optional.of(resolvedReport()));
            given(memberRepository.findById(99L)).willReturn(Optional.of(admin));

            assertThatThrownBy(() -> adminReportService.approveReport(99L, dto))
                    .isInstanceOf(ApiException.class)
                    .extracting(e -> ((ApiException) e).getErrorCode())
                    .isEqualTo(ErrorCode.REPORT_ALREADY_PROCESSED);
        }

        @Test
        @DisplayName("[실패] 이미 REJECTED된 신고를 승인하면 REPORT_ALREADY_PROCESSED 예외")
        void approveReport_alreadyRejected() {
            given(reportRepository.findById(1L)).willReturn(Optional.of(rejectedReport()));
            given(memberRepository.findById(99L)).willReturn(Optional.of(admin));

            assertThatThrownBy(() -> adminReportService.approveReport(99L, dto))
                    .isInstanceOf(ApiException.class)
                    .extracting(e -> ((ApiException) e).getErrorCode())
                    .isEqualTo(ErrorCode.REPORT_ALREADY_PROCESSED);
        }
    }

    // ════════════════════════════════════════════════════════════
    // 신고 반려 (rejectReport)
    // ════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("신고 반려")
    class RejectReportTest {

        @Test
        @DisplayName("[성공] 신고 반려 → rejectReport 호출, 반려 사유 전달")
        void rejectReport_success() {
            Report report = pendingPostReport();
            dto.setRejectReason("허위 신고로 판단됩니다.");

            given(reportRepository.findById(1L)).willReturn(Optional.of(report));
            given(memberRepository.findById(99L)).willReturn(Optional.of(admin));

            adminReportService.rejectReport(99L, dto);

            verify(report).rejectReport(admin, "허위 신고로 판단됩니다.");
        }

        @Test
        @DisplayName("[성공] 반려 사유가 null이어도 정상 처리된다")
        void rejectReport_nullRejectReason() {
            Report report = pendingPostReport();
            dto.setRejectReason(null);

            given(reportRepository.findById(1L)).willReturn(Optional.of(report));
            given(memberRepository.findById(99L)).willReturn(Optional.of(admin));

            adminReportService.rejectReport(99L, dto);

            verify(report).rejectReport(admin, null);
        }

        @Test
        @DisplayName("[실패] 신고가 존재하지 않으면 REPORT_NOT_FOUND 예외")
        void rejectReport_reportNotFound() {
            given(reportRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> adminReportService.rejectReport(99L, dto))
                    .isInstanceOf(ApiException.class)
                    .extracting(e -> ((ApiException) e).getErrorCode())
                    .isEqualTo(ErrorCode.REPORT_NOT_FOUND);
        }

        @Test
        @DisplayName("[실패] 관리자가 존재하지 않으면 MEMBER_NOT_FOUND 예외")
        void rejectReport_adminNotFound() {
            given(reportRepository.findById(1L)).willReturn(Optional.of(pendingPostReport()));
            given(memberRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> adminReportService.rejectReport(99L, dto))
                    .isInstanceOf(ApiException.class)
                    .extracting(e -> ((ApiException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
        }

        @Test
        @DisplayName("[실패] 이미 RESOLVED된 신고를 반려하면 REPORT_ALREADY_PROCESSED 예외")
        void rejectReport_alreadyResolved() {
            given(reportRepository.findById(1L)).willReturn(Optional.of(resolvedReport()));
            given(memberRepository.findById(99L)).willReturn(Optional.of(admin));

            assertThatThrownBy(() -> adminReportService.rejectReport(99L, dto))
                    .isInstanceOf(ApiException.class)
                    .extracting(e -> ((ApiException) e).getErrorCode())
                    .isEqualTo(ErrorCode.REPORT_ALREADY_PROCESSED);
        }

        @Test
        @DisplayName("[실패] 이미 REJECTED된 신고를 다시 반려하면 REPORT_ALREADY_PROCESSED 예외")
        void rejectReport_alreadyRejected() {
            given(reportRepository.findById(1L)).willReturn(Optional.of(rejectedReport()));
            given(memberRepository.findById(99L)).willReturn(Optional.of(admin));

            assertThatThrownBy(() -> adminReportService.rejectReport(99L, dto))
                    .isInstanceOf(ApiException.class)
                    .extracting(e -> ((ApiException) e).getErrorCode())
                    .isEqualTo(ErrorCode.REPORT_ALREADY_PROCESSED);
        }
    }
}