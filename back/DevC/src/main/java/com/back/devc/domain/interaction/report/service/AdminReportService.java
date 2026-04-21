package com.back.devc.domain.interaction.report.service;

import com.back.devc.domain.interaction.notification.service.NotificationService;
import com.back.devc.domain.interaction.report.dto.AdminReportRequestDTO;
import com.back.devc.domain.interaction.report.dto.ReportGroupResponseDTO;
import com.back.devc.domain.interaction.report.dto.ReportResponseDTO;
import com.back.devc.domain.interaction.report.entity.*;
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

    /* =========================================================
     * 1. 단건 신고 조회
     * ========================================================= */
    @Transactional(readOnly = true)
    public Page<ReportResponseDTO> getReports(ReportStatus status, Pageable pageable) {
        Page<Report> reports = (status == null)
                ? reportRepository.findAll(pageable)
                : reportRepository.findAllByStatus(status, pageable);

        return reports.map(this::toDtoWithTargetInfo);
    }

    /* =========================================================
     * 2. 단건 승인
     * ========================================================= */
    public void approveReport(Long adminId, AdminReportRequestDTO dto) {
        Report report = findReportOrThrow(dto.reportId());
        Member admin = findMemberOrThrow(adminId);

        validatePendingStatus(report);

        report.processReport(admin);
        handleTargetAction(report, admin, true, dto.sanctionType(), dto.suspensionDays());
    }

    /* =========================================================
     * 3. 단건 반려
     * ========================================================= */
    public void rejectReport(Long adminId, AdminReportRequestDTO dto) {
        Report report = findReportOrThrow(dto.reportId());
        Member admin = findMemberOrThrow(adminId);

        validatePendingStatus(report);

        report.rejectReport(admin);
        handleTargetAction(report, admin, false, null, null);
    }

    /* =========================================================
     * 4. 그룹 조회
     * ========================================================= */
    @Transactional(readOnly = true)
    public Page<ReportGroupResponseDTO> getGroupedReports(ReportStatus status, Pageable pageable) {
        Page<Object[]> result = reportRepository.findGroupedReports(status, pageable);

        return result.map(row -> {

            TargetType targetType = (TargetType) row[0];
            Long targetId = (Long) row[1];
            Long reportCount = (Long) row[2];
            LocalDateTime latestCreatedAt = (LocalDateTime) row[3];

            String targetNickname = null;
            String targetTitle = null;
            String targetContent = null;

            List<String> reasonTypes = new ArrayList<>();
            List<Report> reports =
                    reportRepository.findAllByTargetTypeAndTargetId(targetType, targetId);

            for (Report r : reports) {
                reasonTypes.add(r.getReasonType());
            }

            if (targetType == TargetType.POST) {
                Post post = postRepository.findById(targetId).orElse(null);
                if (post != null) {
                    targetNickname = post.getMember().getNickname();
                    targetTitle = post.getTitle();
                    targetContent = post.getContent();
                }
            } else if (targetType == TargetType.COMMENT) {
                Comment comment = commentRepository.findById(targetId).orElse(null);
                if (comment != null) {
                    targetContent = comment.getContent();
                    Member m = memberRepository.findById(comment.getUserId()).orElse(null);
                    if (m != null) targetNickname = m.getNickname();
                }
            }

            return new ReportGroupResponseDTO(
                    targetType,
                    targetId,
                    targetNickname,
                    targetTitle,
                    targetContent,
                    reportCount,
                    reasonTypes,
                    ReportStatus.PENDING,
                    latestCreatedAt
            );
        });
    }

    /* =========================================================
     * 5. 그룹 승인
     * ========================================================= */
    public void approveReportGroup(Long adminId, AdminReportRequestDTO dto) {
        Member admin = findMemberOrThrow(adminId);

        TargetType targetType = dto.targetType();
        Long targetId = dto.reportId();

        List<Report> reports =
                reportRepository.findAllByTargetTypeAndTargetIdAndStatus(
                        targetType, targetId, ReportStatus.PENDING);

        if (reports.isEmpty()) return;

        for (Report report : reports) {
            report.processReport(admin);
        }

        handleGroupTargetAction(targetType, targetId, admin, true,
                dto.sanctionType(), dto.suspensionDays());
    }

    /* =========================================================
     * 6. 그룹 반려
     * ========================================================= */
    public void rejectReportGroup(Long adminId, AdminReportRequestDTO dto) {
        Member admin = findMemberOrThrow(adminId);

        TargetType targetType = dto.targetType();
        Long targetId = dto.reportId();

        List<Report> reports =
                reportRepository.findAllByTargetTypeAndTargetIdAndStatus(
                        targetType, targetId, ReportStatus.PENDING);

        if (reports.isEmpty()) return;

        for (Report report : reports) {
            report.rejectReport(admin);
        }

        handleGroupTargetAction(targetType, targetId, admin,
                false, null, null);
    }

    /* =========================================================
     * 공통 처리
     * ========================================================= */

    private void handleTargetAction(Report report, Member admin, boolean approved,
                                    SanctionType sanctionType, Integer suspensionDays) {

        notify(report, admin);
        TargetType targetType = TargetType.valueOf(report.getTargetType());
        if (approved) {
            deleteTarget(targetType, report.getTargetId());
            applyTargetMemberSanction(
                    targetType,
                    report.getTargetId(),
                    sanctionType,
                    suspensionDays
            );
        }
    }

    private void handleGroupTargetAction(TargetType targetType, Long targetId, Member admin,
                                         boolean approved, SanctionType sanctionType, Integer suspensionDays) {

        notifyGroup(targetType, targetId, admin);

        if (approved) {
            deleteTarget(targetType, targetId);
            applyTargetMemberSanction(targetType, targetId, sanctionType, suspensionDays);
        }
    }

    /* =========================================================
     * Target 처리
     * ========================================================= */

    private void deleteTarget(TargetType targetType, Long targetId) {
        if (targetType == TargetType.POST) {
            postRepository.findById(targetId).ifPresent(post -> {
                if (!post.isDeleted()) post.delete();
            });
        } else if (targetType == TargetType.COMMENT) {
            commentRepository.findById(targetId).ifPresent(comment -> {
                if (!comment.isDeleted()) comment.softDelete();
            });
        }
    }

    private void applyTargetMemberSanction(TargetType targetType, Long targetId,
                                           SanctionType sanctionType, Integer suspensionDays) {

        if (sanctionType == null) return;

        Member member = null;

        if (targetType == TargetType.POST) {
            Post post = postRepository.findById(targetId).orElse(null);
            if (post != null) member = post.getMember();

        } else if (targetType == TargetType.COMMENT) {
            Comment comment = commentRepository.findById(targetId).orElse(null);
            if (comment != null) {
                member = memberRepository.findById(comment.getUserId()).orElse(null);
            }
        }

        if (member == null) return;

        applySanction(member, sanctionType, suspensionDays);
    }

    private void applySanction(Member member, SanctionType sanctionType, Integer suspensionDays) {
        switch (sanctionType) {
            case WARNED -> {
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
                member.setSuspendedUntil(null);
            }
        }
    }

    /* =========================================================
     * Notification
     * ========================================================= */

    private void notify(Report report, Member admin) {

        TargetType targetType = TargetType.valueOf(report.getTargetType());

        if (targetType == TargetType.POST) {
            notificationService.createPostReportNotification(
                    report.getTargetId(), admin.getUserId());

        } else if (targetType == TargetType.COMMENT) {
            notificationService.createCommentReportNotification(
                    report.getTargetId(), admin.getUserId());
        }
    }

    private void notifyGroup(TargetType targetType, Long targetId, Member admin) {
        if (targetType == TargetType.POST) {
            notificationService.createPostReportNotification(targetId, admin.getUserId());
        } else if (targetType == TargetType.COMMENT) {
            notificationService.createCommentReportNotification(targetId, admin.getUserId());
        }
    }

    /* =========================================================
     * Util
     * ========================================================= */

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

        TargetType targetType = TargetType.valueOf(report.getTargetType());

        if (targetType == TargetType.POST) {
            Post post = postRepository.findById(report.getTargetId()).orElse(null);
            if (post != null) {
                targetNickname = post.getMember().getNickname();
                targetTitle = post.getTitle();
                targetContent = post.getContent();
            }
        } else if (targetType == TargetType.COMMENT) {
            Comment comment = commentRepository.findById(report.getTargetId()).orElse(null);
            if (comment != null) {
                targetNickname = memberRepository.findById(comment.getUserId())
                        .map(Member::getNickname)
                        .orElse(null);
                targetContent = comment.getContent();
            }
        }

        return ReportResponseDTO.of(report, targetNickname, targetTitle, targetContent);
    }
}