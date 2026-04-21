package com.back.devc.domain.interaction.report.service;

import com.back.devc.domain.interaction.report.dto.ReportRequestDTO;
import com.back.devc.domain.interaction.report.entity.Report;
import com.back.devc.domain.interaction.report.entity.TargetType;
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

    /* =========================================================
     * POST 신고
     * ========================================================= */
    public void reportPost(Long reporterId, ReportRequestDTO dto) {

        Member reporter = findMemberOrThrow(reporterId);

        Post post = postRepository.findById(dto.targetId())
                .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

        validateSelfReport(post.getMember().getUserId(), reporterId);
        validateDeleted(post.isDeleted());
        validateDuplicateReport(reporter, TargetType.POST, dto.targetId());

        Report report = Report.builder()
                .reporter(reporter)
                .targetType(TargetType.POST)
                .targetId(dto.targetId())
                .reasonType(dto.reasonType())
                .reasonDetail(dto.reasonDetail())
                .build();

        reportRepository.save(report);

    }

    /* =========================================================
     * COMMENT 신고
     * ========================================================= */
    public void reportComment(Long reporterId, ReportRequestDTO dto) {

        Member reporter = findMemberOrThrow(reporterId);

        Comment comment = commentRepository.findById(dto.targetId())
                .orElseThrow(() -> new ApiException(ErrorCode.COMMENT_NOT_FOUND));

        validateSelfReport(comment.getUserId(), reporterId);
        validateDeleted(comment.isDeleted());
        validateDuplicateReport(reporter, TargetType.COMMENT, dto.targetId());

        Report report = Report.builder()
                .reporter(reporter)
                .targetType(TargetType.COMMENT)
                .targetId(dto.targetId())
                .reasonType(dto.reasonType())
                .reasonDetail(dto.reasonDetail())
                .build();

        reportRepository.save(report);

    }

    /* =========================================================
     * Validation
     * ========================================================= */

    private Member findMemberOrThrow(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private void validateSelfReport(Long targetUserId, Long reporterId) {
        if (targetUserId.equals(reporterId)) {
            throw new ApiException(ErrorCode.CANNOT_REPORT_SELF);
        }
    }

    private void validateDeleted(boolean deleted) {
        if (deleted) {
            throw new ApiException(ErrorCode.ALREADY_DELETED);
        }
    }

    private void validateDuplicateReport(Member reporter, TargetType type, Long targetId) {
        if (reportRepository.existsByReporterAndTargetTypeAndTargetId(
                reporter, type, targetId)) {
            throw new ApiException(ErrorCode.REPORT_ALREADY_EXISTS);
        }
    }
}