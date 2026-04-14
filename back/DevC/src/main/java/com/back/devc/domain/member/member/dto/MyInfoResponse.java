package com.back.devc.domain.member.member.dto;

import com.back.devc.domain.member.member.entity.MemberRole;
import com.back.devc.domain.member.member.entity.MemberStatus;

public record MyInfoResponse(
        Long userId,
        String email,
        String nickname,
        MemberRole role,
        MemberStatus status
) {
}
