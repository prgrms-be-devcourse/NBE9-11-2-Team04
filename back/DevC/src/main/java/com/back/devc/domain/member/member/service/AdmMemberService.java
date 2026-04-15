package com.back.devc.domain.member.member.service;

import com.back.devc.domain.member.member.dto.AdmMemberStatusUpdateRequest;
import com.back.devc.domain.member.member.dto.AdmMemberDetailResponse;
import com.back.devc.domain.member.member.dto.AdmMemberListResponse;
import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.repository.MemberRepository;
import com.back.devc.global.exception.ApiException;
import com.back.devc.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdmMemberService {

    private final MemberRepository memberRepository;

    // 1. 전체 회원 목록 조회 (최신 가입 순)
    public Page<AdmMemberListResponse> getMembers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return memberRepository.findAll(pageable)
                .map(AdmMemberListResponse::from);
    }

    // 2. 회원 상세 조회
    public AdmMemberDetailResponse getMemberDetail(Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));
        return AdmMemberDetailResponse.from(member);
    }

    // 3. 회원 상태 변경
    @Transactional
    public AdmMemberDetailResponse updateMemberStatus(Long userId, AdmMemberStatusUpdateRequest request) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));
        member.updateStatus(request.status());
        return AdmMemberDetailResponse.from(member);
    }

    // 4. 회원 검색 (닉네임 또는 이메일)
    public Page<AdmMemberListResponse> searchMembers(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return memberRepository.searchByKeyword(keyword, pageable)
                .map(AdmMemberListResponse::from);
    }
}