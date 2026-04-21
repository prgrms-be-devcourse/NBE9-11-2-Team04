package com.back.devc.domain.interaction.report.service;

import com.back.devc.domain.interaction.notification.service.NotificationService;
import com.back.devc.domain.interaction.report.dto.ReportResponseDTO;
import com.back.devc.domain.interaction.report.entity.Report;
import com.back.devc.domain.interaction.report.entity.SanctionType;
import com.back.devc.domain.interaction.report.entity.TargetType;
import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.entity.MemberStatus;
import com.back.devc.domain.member.member.repository.MemberRepository;
import com.back.devc.domain.post.comment.entity.Comment;
import com.back.devc.domain.post.comment.repository.CommentRepository;
import com.back.devc.domain.post.post.entity.Post;
import com.back.devc.domain.post.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
@Service
@RequiredArgsConstructor
@Transactional
public class ReportTargetHandler {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    public void handleApproved(TargetType targetType, Long targetId,
                               Member admin, SanctionType sanctionType, Integer suspensionDays) {

        notify(targetType, targetId, admin);
        deleteTarget(targetType, targetId);

        if (sanctionType != null) {
            applySanction(targetType, targetId, sanctionType, suspensionDays);
        }
    }

    public void handleRejected(TargetType targetType, Long targetId, Member admin) {
        notify(targetType, targetId, admin);
    }

    /* =========================================================
     * 그룹 조회용 TargetInfo
     * ========================================================= */
    public TargetInfo getTargetInfo(TargetType targetType, Long targetId) {

        if (targetType == TargetType.POST) {
            Post post = postRepository.findById(targetId).orElse(null);
            if (post == null) return new TargetInfo(null, null, null);

            return new TargetInfo(
                    post.getMember().getNickname(),
                    post.getTitle(),
                    post.getContent()
            );
        }

        if (targetType == TargetType.COMMENT) {
            Comment comment = commentRepository.findById(targetId).orElse(null);
            if (comment == null) return new TargetInfo(null, null, null);

            String nickname = memberRepository.findById(comment.getUserId())
                    .map(Member::getNickname)
                    .orElse(null);

            return new TargetInfo(
                    nickname,
                    null,
                    comment.getContent()
            );
        }

        return new TargetInfo(null, null, null);
    }

    /* =========================================================
     * 단건 조회 DTO 변환
     * ========================================================= */
    public ReportResponseDTO toDtoWithTargetInfo(Report report) {

        TargetInfo info = getTargetInfo(report.getTargetType(), report.getTargetId());

        return ReportResponseDTO.of(
                report,
                info.nickname(),
                info.title(),
                info.content()
        );
    }

    /* =========================================================
     * Target 처리
     * ========================================================= */
    private void deleteTarget(TargetType targetType, Long targetId) {

        if (targetType == TargetType.POST) {
            postRepository.findById(targetId).ifPresent(post -> {
                if (!post.isDeleted()) post.delete();
            });
        }

        if (targetType == TargetType.COMMENT) {
            commentRepository.findById(targetId).ifPresent(comment -> {
                if (!comment.isDeleted()) comment.softDelete();
            });
        }
    }

    private void applySanction(TargetType targetType, Long targetId,
                               SanctionType sanctionType, Integer suspensionDays) {

        Member member = findTargetMember(targetType, targetId);
        if (member == null) return;

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

    private Member findTargetMember(TargetType targetType, Long targetId) {

        if (targetType == TargetType.POST) {
            Post post = postRepository.findById(targetId).orElse(null);
            if (post != null) return post.getMember();
        }

        if (targetType == TargetType.COMMENT) {
            Comment comment = commentRepository.findById(targetId).orElse(null);
            if (comment != null) {
                return memberRepository.findById(comment.getUserId()).orElse(null);
            }
        }

        return null;
    }

    /* =========================================================
     * Notification
     * ========================================================= */
    private void notify(TargetType targetType, Long targetId, Member admin) {

        if (targetType == TargetType.POST) {
            notificationService.createPostReportNotification(targetId, admin.getUserId());
        } else if (targetType == TargetType.COMMENT) {
            notificationService.createCommentReportNotification(targetId, admin.getUserId());
        }
    }

    /* =========================================================
     * 내부 record
     * ========================================================= */
    public record TargetInfo(
            String nickname,
            String title,
            String content
    ) {}
}