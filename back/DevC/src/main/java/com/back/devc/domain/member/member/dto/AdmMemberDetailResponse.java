package com.back.devc.domain.member.member.dto;

import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.entity.MemberRole;
import com.back.devc.domain.member.member.entity.MemberStatus;
import java.time.LocalDateTime;

public record AdmMemberDetailResponse(
        Long userId,
        String email,
        String nickname,
        MemberRole role,
        MemberStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdmMemberDetailResponse from(Member member) {
        return new AdmMemberDetailResponse(
                member.getUserId(),
                member.getEmail(),
                member.getNickname(),
                member.getRole(),
                member.getStatus(),
                member.getCreatedAt(),
                member.getUpdatedAt()
        );
    }
}
