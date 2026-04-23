package com.back.devc.domain.interaction.report.repository;

import com.back.devc.domain.interaction.report.entity.Report;
import com.back.devc.domain.interaction.report.entity.ReportStatus;
import com.back.devc.domain.interaction.report.entity.TargetType;
import com.back.devc.domain.member.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    Page<Report> findAllByStatus(ReportStatus status, Pageable pageable);

    @Query(
            value = """
        SELECT r.targetType as targetType,
               r.targetId as targetId,
               COUNT(r) as reportCount,
               MAX(r.createdAt) as latestCreatedAt
        FROM Report r
        WHERE (:status IS NULL OR r.status = :status)
        GROUP BY r.targetType, r.targetId
        ORDER BY latestCreatedAt DESC
    """,
            countQuery = """
        SELECT COUNT(DISTINCT CONCAT(r.targetType, '-', r.targetId))
        FROM Report r
        WHERE (:status IS NULL OR r.status = :status)
    """
    )
    Page<Object[]> findGroupedReports(@Param("status") ReportStatus status, Pageable pageable);

    List<Report> findAllByTargetTypeAndTargetIdAndStatus(TargetType targetType, Long targetId, ReportStatus reportStatus);

    List<Report> findAllByTargetTypeAndTargetId(TargetType targetType, Long targetId);

    boolean existsByReporterAndTargetTypeAndTargetId(Member reporter, TargetType targetType, Long targetId);

    @Query("""
        SELECT r.targetType, r.targetId, r.reasonType
        FROM Report r
        WHERE (r.targetType = :postType AND r.targetId IN :postIds)
           OR (r.targetType = :commentType AND r.targetId IN :commentIds)
    """)
    List<Object[]> findReasonTypesBatch(
            @Param("postType") TargetType postType,
            @Param("postIds") List<Long> postIds,
            @Param("commentType") TargetType commentType,
            @Param("commentIds") List<Long> commentIds
    );

    // N+1 처리 전 사용한 조회 방법
    List<String> findReasonTypesByTargetId(TargetType targetType, Long targetId);
}