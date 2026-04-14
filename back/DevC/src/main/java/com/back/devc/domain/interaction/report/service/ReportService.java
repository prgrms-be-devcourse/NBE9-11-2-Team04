package com.back.devc.domain.interaction.report.service;

import com.back.devc.domain.interaction.report.dto.ReportRequestDTO;
import com.back.devc.domain.interaction.report.entity.Report;
import com.back.devc.domain.interaction.report.repository.ReportRepository;
import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.repository.MemberRepository;
import com.back.devc.domain.post.post.entity.Post;
import com.back.devc.domain.post.post.repository.PostRepository;
import com.back.devc.domain.comment.comment.entity.Comment;
import com.back.devc.domain.comment.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    /**
     * 게시글 신고
     */
    public void reportPost(Long reporterId, ReportRequestDTO dto) {
        Member reporter = findMemberOrThrow(reporterId);
        Post post = postRepository.findById(dto.getTargetId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));


        // 중복 신고 체크
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

    /**
     * 댓글 신고
     */
    public void reportComment(Long reporterId, ReportRequestDTO dto) {
        Member reporter = findMemberOrThrow(reporterId);
        Comment comment = commentRepository.findById(dto.getTargetId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        // 중복 신고 체크
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

    // --- 공통 편의 메서드 ---

    private Member findMemberOrThrow(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }

    private void validateDuplicateReport(Member reporter, String type, Long targetId) {
        if (reportRepository.existsByReporterAndTargetTypeAndTargetId(reporter, type, targetId)) {
            throw new IllegalStateException("이미 신고 처리가 접수된 " + (type.equals("POST") ? "게시글" : "댓글") + "입니다.");
        }
    }
}