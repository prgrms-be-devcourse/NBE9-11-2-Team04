package com.back.devc.domain.interaction.report.entity;


import com.back.devc.domain.member.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
/*
* 신고는 동일한 사용자가 동일한 대상(게시글 또는 댓글)에 대해 중복 신고하는 것을 방지하기 위해
* reporter_user_id + target_type + target_id 조합에 대해 유니크 제약조건을 설정
 */
@Table(
        name = "reports",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_report_per_target",
                        columnNames = {"reporter_user_id", "target_type", "target_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class) // 생성일 자동 기록을 위해 필요
@ToString(exclude = {"reporter", "processedByAdmin"})
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    // 1. 신고자 (FK: members 테이블의 user_id 참조)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_user_id", nullable = false)
    private Member reporter;

    @Column(name = "target_type", nullable = false, length = 20)
    private String targetType; // POST, COMMENT 구분

    @Column(name = "target_id", nullable = false)
    private Long targetId; // 신고 대상(게시글 또는 댓글)의 PK

    @Column(name = "reason_type", nullable = false, length = 50)
    private String reasonType; // 신고 종류 - ABUSE, SPAM, HATE, ETC

    @Column(name = "reason_detail", columnDefinition = "TEXT")
    private String reasonDetail;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING"; // PENDING, RESOLVED, REJECTED

    // 2. 처리 관리자 (FK: members 테이블의 user_id 참조)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private Member processedByAdmin;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Report(Member reporter, String targetType, Long targetId, String reasonType, String reasonDetail) {
        this.reporter = reporter;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reasonType = reasonType;
        this.reasonDetail = reasonDetail;
        this.status = "PENDING"; // 초기값 강제
    }

    /**
     * 신고 처리 비즈니스 로직 : 상태 변경
     */
    public void processReport(Member admin, String finalStatus) {
        this.processedByAdmin = admin;
        this.status = finalStatus;
        this.processedAt = LocalDateTime.now();
    }
}