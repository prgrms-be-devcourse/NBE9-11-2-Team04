package com.back.devc.domain.interaction.report.service;

import com.back.devc.domain.interaction.notification.service.NotificationService;
import com.back.devc.domain.interaction.report.dto.ReportRequestDTO;
import com.back.devc.domain.interaction.report.entity.Report;
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
    // 신고 저장 성공 후 대상 작성자에게 신고 알림을 생성하기 위해 사용하는 서비스
    private final NotificationService notificationService;

    public void reportPost(Long reporterId, ReportRequestDTO dto) {
        Member reporter = findMemberOrThrow(reporterId);
        Post post = postRepository.findById(dto.getTargetId())
                .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

        // 1. 본인 신고 방지
        if (post.getMember().getUserId().equals(reporterId)) {
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
        // 신고 저장이 끝난 뒤 신고된 게시글의 작성자에게 REPORT 알림을 생성
        // 실제 알림 생성 가능 여부(자기 자신 신고인지, 중복 알림인지 등)는 NotificationService 에서 한 번 더 검증
        notificationService.createPostReportNotification(dto.getTargetId(), reporterId);
    }

    public void reportComment(Long reporterId, ReportRequestDTO dto) {
        Member reporter = findMemberOrThrow(reporterId);
        Comment comment = commentRepository.findById(dto.getTargetId())
                .orElseThrow(() -> new ApiException(ErrorCode.COMMENT_NOT_FOUND));

        // 1. 본인 신고 방지
        if (comment.getUserId().equals(reporterId)) {
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
        // 신고 저장이 끝난 뒤 신고된 댓글의 작성자에게 REPORT 알림을 생성
        // 실제 알림 생성 가능 여부(자기 자신 신고인지, 중복 알림인지 등)는 NotificationService 에서 한 번 더 검증
        notificationService.createCommentReportNotification(dto.getTargetId(), reporterId);
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