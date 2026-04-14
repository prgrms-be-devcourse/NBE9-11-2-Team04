package com.back.devc.domain.interaction.report.service;

import com.back.devc.domain.interaction.report.dto.AdminReportRequestDTO;
import com.back.devc.domain.interaction.report.dto.ReportResponseDTO;
import com.back.devc.domain.interaction.report.entity.Report;
import com.back.devc.domain.interaction.report.repository.ReportRepository;
import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.repository.MemberRepository;
import com.back.devc.global.exception.ApiException;
import com.back.devc.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminReportService {

    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;

    /**
     * 대기 중인 신고 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ReportResponseDTO> getPendingReports() {
        // status가 PENDING인 것들을 가져와서 DTO로 변환
        return reportRepository.findAllByStatus("PENDING").stream()
                .map(ReportResponseDTO::from)
                .toList();
    }

    /**
     * 신고 승인 (처리)
     */
    public void approveReport(Long adminId, AdminReportRequestDTO dto) {
        Report report = findReportOrThrow(dto.getReportId());
        Member admin = findMemberOrThrow(adminId);

        // 엔티티 내 로직 호출 (상태 RESOLVED로 변경)
        report.processReport(admin, "RESOLVED");

        // TODO: 여기서 피신고자(작성자)에게 경고를 주는 로직을 추가하면 됩니다.
    }

    /**
     * 신고 반려
     */
    public void rejectReport(Long adminId, AdminReportRequestDTO dto) {
        Report report = findReportOrThrow(dto.getReportId());
        Member admin = findMemberOrThrow(adminId);

        report.processReport(admin, "REJECTED");
    }

    private Report findReportOrThrow(Integer reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new ApiException(ErrorCode.REPORT_NOT_FOUND));
    }

    private Member findMemberOrThrow(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));
    }
}