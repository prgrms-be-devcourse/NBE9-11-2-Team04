// AdminMemberListResponse.java
package com.back.devc.domain.member.member.dto;

import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.entity.MemberStatus;

public record AdmMemberListResponse(
        Long userId,
        String email,
        String nickname,
        MemberStatus status
) {
    public static AdmMemberListResponse from(Member member) {
        return new AdmMemberListResponse(
                member.getUserId(),
                member.getEmail(),
                member.getNickname(),
                member.getStatus()
        );
    }
}