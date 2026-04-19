package com.back.devc.domain.interaction.report.service;

import com.back.devc.domain.interaction.notification.service.NotificationService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminReportService {

    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    // 신고가 관리자에 의해 처리된 뒤, 신고 대상 작성자에게 결과 알림을 보내기 위해 사용하는 서비스
    private final NotificationService notificationService;

    /**
     * 대기 중인 신고 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<ReportResponseDTO> getPendingReports(Pageable pageable) {
        return reportRepository.findAllByStatus("PENDING", pageable)
                .map(this::toDtoWithTargetInfo);
    }

    /**
     * 신고 승인 처리
     * 1. 신고 상태를 RESOLVED로 변경
     * 2. 대상 콘텐츠(게시글 또는 댓글) soft delete
     * 3. 신고 대상 회원의 status 제재 처리 (ACTIVE → WARNED → BLACKLISTED)
     */
    public void approveReport(Long adminId, AdminReportRequestDTO dto) {
        Report report = findReportOrThrow(dto.getReportId());
        Member admin = findMemberOrThrow(adminId);

        // 이미 처리된 신고인지 검증
        validatePendingStatus(report);

        // 1. 신고 상태 변경 (RESOLVED)
        report.processReport(admin, "RESOLVED");
        // 관리자 승인 처리 후, 신고 대상 작성자에게 신고 처리 결과 알림을 생성
        if ("POST".equals(report.getTargetType())) {
            notificationService.createPostReportNotification(report.getTargetId(), adminId);
        }
        if ("COMMENT".equals(report.getTargetType())) {
            notificationService.createCommentReportNotification(report.getTargetId(), adminId);
        }

        // 2. 대상 콘텐츠 삭제 및 작성자 제재
        if ("POST".equals(report.getTargetType())) {
            Post post = postRepository.findById(report.getTargetId())
                    .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

            // soft delete
            post.delete();
            sanctionMember(post.getMember());

        } else if ("COMMENT".equals(report.getTargetType())) {
            Comment comment = commentRepository.findById(report.getTargetId())
                    .orElseThrow(() -> new ApiException(ErrorCode.COMMENT_NOT_FOUND));

            comment.softDelete();

            // 작성자 제재
            // 댓글 작성자는 userId로 관리되므로 UserId로 조회
            Member commentAuthor = memberRepository.findById(comment.getUserId())
                    .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));
            sanctionMember(commentAuthor);
        }
    }

    /**
     * 신고 반려 처리
     */
    public void rejectReport(Long adminId, AdminReportRequestDTO dto) {
        Report report = findReportOrThrow(dto.getReportId());
        Member admin = findMemberOrThrow(adminId);

        validatePendingStatus(report);

        report.processReport(admin, "REJECTED");
        // 관리자 반려 처리 후에도, 신고 대상 작성자에게 신고 처리 결과 알림을 생성
        if ("POST".equals(report.getTargetType())) {
            notificationService.createPostReportNotification(report.getTargetId(), adminId);
        }
        if ("COMMENT".equals(report.getTargetType())) {
            notificationService.createCommentReportNotification(report.getTargetId(), adminId);
        }
    }

    /**
     * 회원 제재 처리
     * ACTIVE → WARNED → BLACKLISTED 순으로 단계 상승
     */
    private void sanctionMember(Member member) {
        MemberStatus currentStatus = member.getStatus();

        if (currentStatus == MemberStatus.ACTIVE) {
            member.updateStatus(MemberStatus.WARNED);
        } else if (currentStatus == MemberStatus.WARNED) {
            member.updateStatus(MemberStatus.BLACKLISTED);
        }
        // BLACKLISTED는 이미 최고 단계이므로 추가 변경 없음
    }

    /**
     * PENDING 상태인지 검증 (중복 처리 방지)
     */
    private void validatePendingStatus(Report report) {
        if (!"PENDING".equals(report.getStatus())) {
            throw new ApiException(ErrorCode.REPORT_ALREADY_PROCESSED);
        }
    }

    private Report findReportOrThrow(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new ApiException(ErrorCode.REPORT_NOT_FOUND));
    }

    private Member findMemberOrThrow(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));
    }


    private ReportResponseDTO toDtoWithTargetInfo(Report report) {

        String targetNickname = null;
        String targetTitle = null;
        String targetContent = null;

        if ("POST".equals(report.getTargetType())) {
            Post post = postRepository.findById(report.getTargetId())
                    .orElse(null);

            if (post != null) {
                targetNickname = post.getMember().getNickname();
                targetTitle = post.getTitle();
                targetContent = post.getContent();
            }
        }

        if ("COMMENT".equals(report.getTargetType())) {
            Comment comment = commentRepository.findById(report.getTargetId())
                    .orElse(null);

            if (comment != null) {
                targetNickname = comment.getId().toString(); // nickname 가져오려면 service 단에서 userId로 member 조회 필요
                targetContent = comment.getContent();
            }
        }

        return ReportResponseDTO.of(
                report,
                targetNickname,
                targetTitle,
                targetContent
        );
    }
}