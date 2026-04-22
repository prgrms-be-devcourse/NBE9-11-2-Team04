package com.back.devc.domain.interaction.report.service;

import com.back.devc.domain.interaction.report.dto.AdminReportRequestDTO;
import com.back.devc.domain.interaction.report.dto.ReportGroupResponseDTO;
import com.back.devc.domain.interaction.report.dto.ReportResponseDTO;
import com.back.devc.domain.interaction.report.entity.Report;
import com.back.devc.domain.interaction.report.entity.ReportStatus;
import com.back.devc.domain.interaction.report.entity.TargetType;
import com.back.devc.domain.interaction.report.repository.ReportRepository;
import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.repository.MemberRepository;
import com.back.devc.global.exception.ApiException;
import com.back.devc.global.exception.ErrorCode;
import com.back.devc.global.exception.errorCode.MemberErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
@Transactional
public class AdminReportService {

    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;
    private final ReportTargetHandler reportTargetHandler;

    /* =========================================================
     * 1. 단건 신고 조회
     * ========================================================= */
    @Transactional(readOnly = true)
    public Page<ReportResponseDTO> getReports(ReportStatus status, Pageable pageable) {

        Page<Report> reports = (status == null)
                ? reportRepository.findAll(pageable)
                : reportRepository.findAllByStatus(status, pageable);

        return reports.map(reportTargetHandler::toDtoWithTargetInfo);
    }

    /* =========================================================
     * 2. 그룹 조회
     * ========================================================= */
    @Transactional(readOnly = true)
    public Page<ReportGroupResponseDTO> getGroupedReports(ReportStatus status, Pageable pageable) {

        Page<Object[]> result = reportRepository.findGroupedReports(status, pageable);

        return result.map(row -> {

            TargetType targetType = (TargetType) row[0];
            Long targetId = (Long) row[1];
            Long reportCount = (Long) row[2];
            LocalDateTime latestCreatedAt = (LocalDateTime) row[3];

            // target 정보 조회 (중복 제거)
            ReportTargetHandler.TargetInfo info =
                    reportTargetHandler.getTargetInfo(targetType, targetId);

            // reasonTypes 조회
            List<String> reasonTypes =
                    reportRepository.findReasonTypesByTarget(targetType, targetId);

            return new ReportGroupResponseDTO(
                    targetType,
                    targetId,
                    info.nickname(),
                    info.title(),
                    info.content(),
                    reportCount,
                    reasonTypes,
                    ReportStatus.PENDING,
                    latestCreatedAt
            );
        });
    }

    /* =========================================================
     * 3. 단건 승인
     * ========================================================= */
    public void approveReport(Long adminId, AdminReportRequestDTO dto) {

        Member admin = findMemberOrThrow(adminId);
        Report report = findReportOrThrow(dto.reportId());

        validatePendingStatus(report);

        report.processReport(admin);

        reportTargetHandler.handleApproved(
                report.getTargetType(),
                report.getTargetId(),
                admin,
                dto.sanctionType(),
                dto.suspensionDays()
        );
    }

    /* =========================================================
     * 4. 단건 반려
     * ========================================================= */
    public void rejectReport(Long adminId, AdminReportRequestDTO dto) {

        Member admin = findMemberOrThrow(adminId);
        Report report = findReportOrThrow(dto.reportId());

        validatePendingStatus(report);

        report.rejectReport(admin);

        reportTargetHandler.handleRejected(
                report.getTargetType(),
                report.getTargetId(),
                admin
        );
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
                        targetType, targetId, ReportStatus.PENDING
                );

        if (reports.isEmpty()) return;

        reports.forEach(r -> r.processReport(admin));

        reportTargetHandler.handleApproved(
                targetType,
                targetId,
                admin,
                dto.sanctionType(),
                dto.suspensionDays()
        );
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
                        targetType, targetId, ReportStatus.PENDING
                );

        if (reports.isEmpty()) return;

        reports.forEach(r -> r.rejectReport(admin));

        reportTargetHandler.handleRejected(targetType, targetId, admin);
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
                .orElseThrow(() -> new ApiException(MemberErrorCode.MEMBER_NOT_FOUND));
    }
}