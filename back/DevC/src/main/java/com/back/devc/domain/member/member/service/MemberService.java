package com.back.devc.domain.member.member.service;

import com.back.devc.domain.member.member.dto.MyInfoResponse;
import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.entity.MemberStatus;
import com.back.devc.domain.member.member.repository.MemberRepository;
import com.back.devc.global.exception.ApiException;
import com.back.devc.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    // 회원 정보 조회
    @Transactional(readOnly = true)
    public MyInfoResponse getMyInfo(Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));

        return new MyInfoResponse(
                member.getUserId(),
                member.getEmail(),
                member.getNickname(),
                member.getRole(),
                member.getStatus(),
                member.getCreatedAt()
        );
    }

    // 회원 탈퇴
    @Transactional
    public void withdraw(Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));

        // 상태를 WITHDRAWN으로 변경
        member.updateStatus(MemberStatus.WITHDRAWN);

        // 회원 탈퇴 후 DB에서 삭제 (원한다면 활성화)
        memberRepository.delete(member);
    }
}