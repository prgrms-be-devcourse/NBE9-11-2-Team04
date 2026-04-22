package com.back.devc.domain.interaction.report.service;

import com.back.devc.domain.interaction.report.dto.AdminReportRequestDTO;
import com.back.devc.domain.interaction.report.dto.ReportGroupResponseDTO;
import com.back.devc.domain.interaction.report.dto.ReportResponseDTO;
import com.back.devc.domain.interaction.report.entity.Report;
import com.back.devc.domain.interaction.report.entity.ReportStatus;
import com.back.devc.domain.interaction.report.entity.TargetType;
import com.back.devc.domain.interaction.report.repository.ReportRepository;
import com.back.devc.domain.interaction.report.util.ReportTargetHandler;
import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.repository.MemberRepository;
import com.back.devc.global.exception.ApiException;
import com.back.devc.global.exception.ErrorCode;
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
        validateAdminRole(admin); // [추가] 관리자 권한 확인

        Report report = findReportOrThrow(dto.reportId());
        validatePendingStatus(report);

        // [추가] 신고 대상이 현재 존재하는지 확인 (이미 삭제된 게시글 등 방지)
        validateTargetExists(report.getTargetType(), report.getTargetId());

        // [추가] 제재 옵션 유효성 검사 (예: 정지인데 기간이 0인 경우)
        validateSanctionDetails(dto);

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
        validateAdminRole(admin); // [추가] 관리자 권한 확인

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
        validateAdminRole(admin);

        TargetType targetType = dto.targetType();
        Long targetId = dto.reportId(); // DTO 구조상 reportId를 targetId로 사용 중인 점 확인 필요

        validateTargetExists(targetType, targetId);
        validateSanctionDetails(dto);

        List<Report> reports = reportRepository.findAllByTargetTypeAndTargetIdAndStatus(
                targetType, targetId, ReportStatus.PENDING);

        if (reports.isEmpty()) {
            // [추가] 처리할 대기 중인 신고가 없는 경우 에러 처리
            throw new ApiException(ErrorCode.NO_PENDING_REPORTS_FOUND);
        }

        reports.forEach(r -> r.processReport(admin));

        reportTargetHandler.handleApproved(
                targetType, targetId, admin, dto.sanctionType(), dto.suspensionDays()
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

    private void validateAdminRole(Member member) {
        // [에러] 사용자가 관리자 권한을 가지고 있지 않은 경우
        if (!member.isAdmin()) { // Member 엔티티에 isAdmin() 또는 Role 확인 로직 필요
            throw new ApiException(ErrorCode.UNAUTHORIZED_ADMIN_ACCESS);
        }
    }

    private void validateTargetExists(TargetType type, Long targetId) {
        // [에러] 신고 대상(게시글, 댓글 등)이 이미 삭제되어 존재하지 않는 경우
        if (!reportTargetHandler.exists(type, targetId)) {
            throw new ApiException(ErrorCode.REPORT_TARGET_NOT_FOUND);
        }
    }

    private void validateSanctionDetails(AdminReportRequestDTO dto) {
        // [에러] 제재 유형이 '정지'인데 정지 일수가 누락되었거나 부적절한 경우
        if ("SUSPENSION".equals(dto.sanctionType()) && (dto.suspensionDays() == null || dto.suspensionDays() <= 0)) {
            throw new ApiException(ErrorCode.INVALID_SANCTION_PARAMETER);
        }
    }

    private void validatePendingStatus(Report report) {
        // [에러] 이미 승인 또는 반려 처리가 완료된 신고인 경우
        if (report.getStatus() != ReportStatus.PENDING) {
            throw new ApiException(ErrorCode.REPORT_ALREADY_PROCESSED);
        }
    }

    private Report findReportOrThrow(Long reportId) {
        // [에러] 신고 ID 자체가 존재하지 않는 경우
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new ApiException(ErrorCode.REPORT_NOT_FOUND));
    }

    private Member findMemberOrThrow(Long userId) {
        // [에러] 요청한 관리자 ID가 사용자 DB에 없는 경우
        return memberRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));
    }
}