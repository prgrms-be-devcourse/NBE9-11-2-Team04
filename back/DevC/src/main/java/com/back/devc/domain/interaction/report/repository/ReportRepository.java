package com.back.devc.domain.interaction.report.repository;

import com.back.devc.domain.interaction.report.entity.Report;
import com.back.devc.domain.member.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    boolean existsByReporterAndTargetTypeAndTargetId(Member reporter, String post, Long postId);

    Page<Report> findAllByStatus(String status, Pageable pageable);}
