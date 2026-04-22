package com.back.devc.domain.member.member.service;

import com.back.devc.domain.member.member.dto.AdmMemberDetailResponse;
import com.back.devc.domain.member.member.dto.AdmMemberListRequest;
import com.back.devc.domain.member.member.dto.AdmMemberListResponse;
import com.back.devc.domain.member.member.dto.AdmMemberStatusUpdateRequest;
import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.entity.MemberStatus;
import com.back.devc.domain.member.member.repository.MemberRepository;
import com.back.devc.domain.post.comment.repository.CommentRepository;
import com.back.devc.domain.post.post.repository.PostRepository;
import com.back.devc.global.exception.ApiException;
import com.back.devc.global.exception.ErrorCode;
import com.back.devc.global.exception.errorCode.MemberErrorCode;
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
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    // 1. 목록 조회
    public Page<AdmMemberListResponse> getMembers(AdmMemberListRequest request) {

        Pageable pageable = PageRequest.of(
                request.page(),
                request.size(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Member> members = findMembers(request, pageable);

        return members.map(member -> {
            long postCount = postRepository.countByMember(member);
            long commentCount = commentRepository.countByUserIdAndIsDeletedFalse(member.getUserId());

            return AdmMemberListResponse.of(member, postCount, commentCount);
        });
    }

    // 2. 상세 조회
    public AdmMemberDetailResponse getMemberDetail(Long userId) {
        Member member = findMemberOrThrow(userId);
        return convertToDetailResponse(member);
    }

    // 3. 상태 변경
    @Transactional
    public AdmMemberDetailResponse updateMemberStatus(Long userId,
                                                      AdmMemberStatusUpdateRequest request) {

        Member member = findMemberOrThrow(userId);
        member.updateStatus(request.status());

        return convertToDetailResponse(member);
    }

    /*
     * 내부 로직
     */
    private Page<Member> findMembers(AdmMemberListRequest request, Pageable pageable) {

        String keyword = request.keyword();
        MemberStatus status = request.status();

        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasStatus = status != null;

        if (!hasKeyword && !hasStatus) {
            return memberRepository.findAll(pageable);
        }

        if (hasKeyword && !hasStatus) {
            return memberRepository.findByNicknameContainingOrEmailContaining(keyword, keyword, pageable);
        }

        if (!hasKeyword) {
            return memberRepository.findByStatus(status, pageable);
        }

        return memberRepository.findByStatusAndNicknameContainingOrStatusAndEmailContaining(
                status, keyword,
                status, keyword,
                pageable
        );
    }

    private AdmMemberDetailResponse convertToDetailResponse(Member member) {
        long postCount = postRepository.countByMember(member);
        long commentCount = commentRepository.countByUserIdAndIsDeletedFalse(member.getUserId());

        return AdmMemberDetailResponse.of(member, postCount, commentCount);
    }

    private Member findMemberOrThrow(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));
    }
}