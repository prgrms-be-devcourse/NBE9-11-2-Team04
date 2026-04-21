package com.back.devc.domain.member.member.service;

import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.entity.MemberStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MemberSanctionService {

    // WARN
    public void warn(Member member) {
        if (member.getStatus() == MemberStatus.ACTIVE) {
            member.updateStatus(MemberStatus.WARNED);
        }
    }

    // SUSPEND (기간 있음)
    public void suspend(Member member, Integer days) {
        int validDays = (days != null && days > 0) ? days : 1;

        member.updateStatus(MemberStatus.SUSPENDED);
        member.setSuspendedUntil(
                LocalDateTime.now().plusDays(validDays)
        );
    }

    // BLACKLIST
    public void blacklist(Member member) {
        member.updateStatus(MemberStatus.BLACKLISTED);
        member.setSuspendedUntil(null);
    }

    public void apply(Member member, MemberStatus status, Integer days) {
        switch (status) {
            case WARNED -> warn(member);
            case SUSPENDED -> suspend(member, days);
            case BLACKLISTED -> blacklist(member);
        }
    }
}