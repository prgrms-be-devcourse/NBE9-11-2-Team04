package com.back.devc.domain.interaction.report.service;

import com.back.devc.domain.interaction.notification.service.NotificationService;
import com.back.devc.domain.interaction.report.dto.AdminReportRequestDTO;
import com.back.devc.domain.interaction.report.dto.ReportGroupResponseDTO;
import com.back.devc.domain.interaction.report.dto.ReportResponseDTO;
import com.back.devc.domain.interaction.report.entity.Report;
import com.back.devc.domain.interaction.report.entity.ReportStatus;
import com.back.devc.domain.interaction.report.entity.SanctionType;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminReportService {

    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;

    // 1. 단건 신고 조회
    @Transactional(readOnly = true)
    public Page<ReportResponseDTO> getReports(ReportStatus status, Pageable pageable) {
        Page<Report> reports = (status == null)
                ? reportRepository.findAll(pageable)
                : reportRepository.findAllByStatus(status, pageable);

        return reports.map(this::toDtoWithTargetInfo);
    }

    // 2. 단건 승인 처리
    public void approveReport(Long adminId, AdminReportRequestDTO dto) {
        Report report = findReportOrThrow(dto.reportId());
        Member admin = findMemberOrThrow(adminId);

        validatePendingStatus(report);

        report.processReport(admin);
        handleTargetAction(report, admin, true, dto.sanctionType(), dto.suspensionDays());
    }

    // 3. 단건 반려
    public void rejectReport(Long adminId, AdminReportRequestDTO dto) {
        Report report = findReportOrThrow(dto.reportId());
        Member admin = findMemberOrThrow(adminId);

        validatePendingStatus(report);

        report.rejectReport(admin);
        handleTargetAction(report, admin, false, null, null);
    }

    // 4. 그룹(게시글/댓글 사용자별) 조회
    @Transactional(readOnly = true)
    public Page<ReportGroupResponseDTO> getGroupedReports(ReportStatus status, Pageable pageable) {
        Page<Object[]> result = reportRepository.findGroupedReports(status, pageable);

        return result.map(row -> {
            String targetType = (String) row[0];
            Long targetId = (Long) row[1];
            Long reportCount = (Long) row[2];
            LocalDateTime latestCreatedAt = (LocalDateTime) row[3];

            String targetNickname = null;
            String targetTitle = null;
            String targetContent = null;

            List<String> reasonTypes = new ArrayList<>();
            List<Report> reports = reportRepository.findAllByTargetTypeAndTargetId(targetType, targetId);

            for (Report r : reports) {
                reasonTypes.add(r.getReasonType());
            }

            if ("POST".equals(targetType)) {
                Post post = postRepository.findById(targetId).orElse(null);
                if (post != null) {
                    targetNickname = post.getMember().getNickname();
                    targetTitle = post.getTitle();
                    targetContent = post.getContent();
                }
            } else if ("COMMENT".equals(targetType)) {
                Comment comment = commentRepository.findById(targetId).orElse(null);
                if (comment != null) {
                    targetContent = comment.getContent();
                    Member m = memberRepository.findById(comment.getUserId()).orElse(null);
                    if (m != null) targetNickname = m.getNickname();
                }
            }

            return new ReportGroupResponseDTO(
                    targetType, targetId, targetNickname, targetTitle, targetContent,
                    reportCount, reasonTypes, ReportStatus.PENDING, latestCreatedAt
            );
        });
    }

    // 5. 그룹 승인 처리
    public void approveReportGroup(Long adminId, AdminReportRequestDTO dto) {
        Member admin = findMemberOrThrow(adminId);

        // targetType, targetId는 DTO에서 가져옴 (reportId가 그룹 대표 targetId로 사용됨)
        String targetType = dto.targetType();
        Long targetId = dto.reportId(); // 프론트에서 targetId를 reportId로 전달

        List<Report> reports = reportRepository.findAllByTargetTypeAndTargetIdAndStatus(
                targetType, targetId, ReportStatus.PENDING);

        if (reports.isEmpty()) return;

        for (Report report : reports) {
            report.processReport(admin);
        }

        handleGroupTargetAction(targetType, targetId, admin, true, dto.sanctionType(), dto.suspensionDays());
    }

    // 6. 그룹 반려 처리
    public void rejectReportGroup(Long adminId, AdminReportRequestDTO dto) {
        Member admin = findMemberOrThrow(adminId);

        String targetType = dto.targetType();
        Long targetId = dto.reportId();

        List<Report> reports = reportRepository.findAllByTargetTypeAndTargetIdAndStatus(
                targetType, targetId, ReportStatus.PENDING);

        if (reports.isEmpty()) return;

        for (Report report : reports) {
            report.rejectReport(admin);
        }

        handleGroupTargetAction(targetType, targetId, admin, false, null, null);
    }

    /* =========================================================
     * 공통 처리 로직
     * ========================================================= */

    private void handleTargetAction(Report report, Member admin, boolean approved,
                                    SanctionType sanctionType, Integer suspensionDays) {
        notify(report, admin);
        if (approved) {
            deleteTarget(report.getTargetType(), report.getTargetId());
            applyTargetMemberSanction(report.getTargetType(), report.getTargetId(), sanctionType, suspensionDays);
        }
    }

    private void handleGroupTargetAction(String targetType, Long targetId, Member admin,
                                         boolean approved, SanctionType sanctionType, Integer suspensionDays) {
        notifyGroup(targetType, targetId, admin);
        if (approved) {
            deleteTarget(targetType, targetId);
            applyTargetMemberSanction(targetType, targetId, sanctionType, suspensionDays);
        }
    }

    /**
     * 대상 게시글/댓글을 삭제(soft delete) 처리한다.
     */
    private void deleteTarget(String targetType, Long targetId) {
        if ("POST".equals(targetType)) {
            postRepository.findById(targetId).ifPresent(post -> {
                if (!post.isDeleted()) post.delete();
            });
        } else if ("COMMENT".equals(targetType)) {
            commentRepository.findById(targetId).ifPresent(comment -> {
                if (!comment.isDeleted()) comment.softDelete();
            });
        }
    }

    /**
     * 대상 게시글/댓글의 작성자를 찾아 관리자가 선택한 제재를 직접 적용한다.
     * 자동 escalate 방식을 제거하고 관리자가 명시적으로 선택한 SanctionType을 따른다.
     */
    private void applyTargetMemberSanction(String targetType, Long targetId,
                                           SanctionType sanctionType, Integer suspensionDays) {
        if (sanctionType == null) return;

        Member member = null;

        if ("POST".equals(targetType)) {
            Post post = postRepository.findById(targetId).orElse(null);
            if (post != null) member = post.getMember();
        } else if ("COMMENT".equals(targetType)) {
            Comment comment = commentRepository.findById(targetId).orElse(null);
            if (comment != null) member = memberRepository.findById(comment.getUserId()).orElse(null);
        }

        if (member == null) return;
        applySanction(member, sanctionType, suspensionDays);
    }

    /**
     * 관리자가 선택한 제재 유형에 따라 회원 상태를 직접 설정한다.
     *
     * - WARNED      : 경고 상태로 변경 (이미 상위 제재 상태면 변경하지 않음)
     * - SUSPENDED   : suspensionDays 기간만큼 정지, suspendedUntil 설정
     * - BLACKLISTED : 영구 차단
     */
    private void applySanction(Member member, SanctionType sanctionType, Integer suspensionDays) {
        switch (sanctionType) {
            case WARNED -> {
                // 이미 정지/차단 상태라면 경고로 되돌리지 않는다 (하향 방지)
                if (member.getStatus() == MemberStatus.ACTIVE) {
                    member.updateStatus(MemberStatus.WARNED);
                }
            }
            case SUSPENDED -> {
                int days = (suspensionDays != null && suspensionDays > 0) ? suspensionDays : 1;
                member.updateStatus(MemberStatus.SUSPENDED);
                member.setSuspendedUntil(LocalDateTime.now().plusDays(days));
            }
            case BLACKLISTED -> {
                member.updateStatus(MemberStatus.BLACKLISTED);
                member.setSuspendedUntil(null); // 영구 차단은 기간 불필요
            }
        }
    }

    /* =========================================================
     * 알림 및 편의 메서드
     * ========================================================= */

    private void notify(Report report, Member admin) {
        if ("POST".equals(report.getTargetType())) {
            notificationService.createPostReportNotification(report.getTargetId(), admin.getUserId());
        } else if ("COMMENT".equals(report.getTargetType())) {
            notificationService.createCommentReportNotification(report.getTargetId(), admin.getUserId());
        }
    }

    private void notifyGroup(String targetType, Long targetId, Member admin) {
        if ("POST".equals(targetType)) {
            notificationService.createPostReportNotification(targetId, admin.getUserId());
        } else if ("COMMENT".equals(targetType)) {
            notificationService.createCommentReportNotification(targetId, admin.getUserId());
        }
    }

    private void validatePendingStatus(Report report) {
        if (report.getStatus() != ReportStatus.PENDING) {
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
            Post post = postRepository.findById(report.getTargetId()).orElse(null);
            if (post != null) {
                targetNickname = post.getMember().getNickname();
                targetTitle = post.getTitle();
                targetContent = post.getContent();
            }
        } else if ("COMMENT".equals(report.getTargetType())) {
            Comment comment = commentRepository.findById(report.getTargetId()).orElse(null);
            if (comment != null) {
                targetNickname = memberRepository.findById(comment.getUserId())
                        .map(Member::getNickname).orElse(null);
                targetContent = comment.getContent();
            }
        }

        return ReportResponseDTO.of(report, targetNickname, targetTitle, targetContent);
    }
}