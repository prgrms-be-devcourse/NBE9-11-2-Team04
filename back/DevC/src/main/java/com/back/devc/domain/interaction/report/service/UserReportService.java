package com.back.devc.domain.interaction.report.service;

import com.back.devc.domain.interaction.notification.service.NotificationService;
import com.back.devc.domain.interaction.report.dto.ReportRequestDTO;
import com.back.devc.domain.interaction.report.entity.*;
import com.back.devc.domain.interaction.report.repository.ReportRepository;
import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.repository.MemberRepository;
import com.back.devc.domain.post.comment.entity.Comment;
import com.back.devc.domain.post.comment.repository.CommentRepository;
import com.back.devc.domain.post.post.entity.Post;
import com.back.devc.domain.post.post.repository.PostRepository;
import com.back.devc.global.exception.ApiException;
import com.back.devc.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserReportService {

    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;

    /* =========================================================
     *  Public API: 게시글/댓글 신고 엔드포인트
     * ========================================================= */
    public void reportPost(Long reporterId, ReportRequestDTO dto) {
        report(reporterId, TargetType.POST, dto);
    }

    public void reportComment(Long reporterId, ReportRequestDTO dto) {
        report(reporterId, TargetType.COMMENT, dto);
    }

    /* =========================================================
     * 통합 신고 로직
     * ========================================================= */
    public void report(Long reporterId, TargetType targetType, ReportRequestDTO dto) {

        Member reporter = findMemberOrThrow(reporterId);

        validateTarget(reporterId, targetType, dto.targetId());
        validateDuplicateReport(reporter, targetType, dto.targetId());

        Report report = Report.builder()
                .reporter(reporter)
                .targetType(targetType)
                .targetId(dto.targetId())
                .reasonType(dto.reasonType())
                .reasonDetail(dto.reasonDetail())
                .build();

        reportRepository.save(report);

        notify(targetType, dto.targetId(), reporterId);
    }

    /* =========================================================
     * Validation
     * ========================================================= */

    private void validateTarget(Long reporterId, TargetType type, Long targetId) {

        if (type == TargetType.POST) {
            Post post = postRepository.findById(targetId)
                    .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

            if (post.getMember().getUserId().equals(reporterId)) {
                throw new ApiException(ErrorCode.CANNOT_REPORT_SELF);
            }

            if (post.isDeleted()) {
                throw new ApiException(ErrorCode.ALREADY_DELETED);
            }
        }

        if (type == TargetType.COMMENT) {
            Comment comment = commentRepository.findById(targetId)
                    .orElseThrow(() -> new ApiException(ErrorCode.COMMENT_NOT_FOUND));

            if (comment.getUserId().equals(reporterId)) {
                throw new ApiException(ErrorCode.CANNOT_REPORT_SELF);
            }

            if (comment.isDeleted()) {
                throw new ApiException(ErrorCode.ALREADY_DELETED);
            }
        }
    }

    private void validateDuplicateReport(Member reporter, TargetType type, Long targetId) {
        if (reportRepository.existsByReporterAndTargetTypeAndTargetId(
                reporter, type, targetId)) {
            throw new ApiException(ErrorCode.REPORT_ALREADY_EXISTS);
        }
    }

    /* =========================================================
     * Notification
     * ========================================================= */

    private void notify(TargetType type, Long targetId, Long reporterId) {

        if (type == TargetType.POST) {
            notificationService.createPostReportNotification(targetId, reporterId);
        } else {
            notificationService.createCommentReportNotification(targetId, reporterId);
        }
    }

    private Member findMemberOrThrow(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));
    }
}