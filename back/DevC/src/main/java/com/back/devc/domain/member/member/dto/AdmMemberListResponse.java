// AdminMemberListResponse.java
package com.back.devc.domain.member.member.dto;

import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.entity.MemberRole;
import com.back.devc.domain.member.member.entity.MemberStatus;
import java.time.LocalDateTime;

public record AdmMemberListResponse(
        Long userId,
        String email,
        String nickname,
        MemberRole role,
        MemberStatus status,
        LocalDateTime createdAt
) {
    public static AdmMemberListResponse from(Member member) {
        return new AdmMemberListResponse(
                member.getUserId(),
                member.getEmail(),
                member.getNickname(),
                member.getRole(),
                member.getStatus(),
                member.getCreatedAt()
        );
    }
}