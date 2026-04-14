package com.back.devc.domain.interaction.report.service;

import com.back.devc.domain.comment.comment.entity.Comment;
import com.back.devc.domain.comment.comment.repository.CommentRepository;
import com.back.devc.domain.interaction.report.dto.ReportRequestDTO;
import com.back.devc.domain.interaction.report.entity.Report;
import com.back.devc.domain.interaction.report.repository.ReportRepository;
import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.repository.MemberRepository;
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

    public void reportPost(Long reporterId, ReportRequestDTO dto) {
        Member reporter = findMemberOrThrow(reporterId);
        Post post = postRepository.findById(dto.getTargetId())
                .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

        // 1. 본인 신고 방지
        if (post.getUser().getUserId().equals(reporterId)) {
            throw new ApiException(ErrorCode.CANNOT_REPORT_SELF);
        }

        // 2. 삭제 상태 체크
        if (post.isDeleted()) {
            throw new ApiException(ErrorCode.ALREADY_DELETED);
        }

        // 3. 중복 신고 체크
        validateDuplicateReport(reporter, "POST", dto.getTargetId());

        Report report = Report.builder()
                .reporter(reporter)
                .targetType("POST")
                .targetId(dto.getTargetId())
                .reasonType(dto.getReasonType())
                .reasonDetail(dto.getReasonDetail())
                .build();

        reportRepository.save(report);
    }

    public void reportComment(Long reporterId, ReportRequestDTO dto) {
        Member reporter = findMemberOrThrow(reporterId);
        Comment comment = commentRepository.findById(dto.getTargetId())
                .orElseThrow(() -> new ApiException(ErrorCode.COMMENT_NOT_FOUND));

        // 1. 본인 신고 방지
        if (comment.getUser().getUserId().equals(reporterId)) {
            throw new ApiException(ErrorCode.CANNOT_REPORT_SELF);
        }

        // 2. 삭제 상태 체크
        if (comment.isDeleted()) {
            throw new ApiException(ErrorCode.ALREADY_DELETED);
        }

        // 3. 중복 신고 체크
        validateDuplicateReport(reporter, "COMMENT", dto.getTargetId());

        Report report = Report.builder()
                .reporter(reporter)
                .targetType("COMMENT")
                .targetId(dto.getTargetId())
                .reasonType(dto.getReasonType())
                .reasonDetail(dto.getReasonDetail())
                .build();

        reportRepository.save(report);
    }

    private Member findMemberOrThrow(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private void validateDuplicateReport(Member reporter, String type, Long targetId) {
        if (reportRepository.existsByReporterAndTargetTypeAndTargetId(reporter, type, targetId)) {
            throw new ApiException(ErrorCode.REPORT_ALREADY_EXISTS);
        }
    }
}