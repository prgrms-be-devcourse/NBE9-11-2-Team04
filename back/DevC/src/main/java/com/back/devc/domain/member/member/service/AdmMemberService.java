package com.back.devc.domain.member.member.service;

import com.back.devc.domain.member.member.dto.AdmMemberDetailResponse;
import com.back.devc.domain.member.member.dto.AdmMemberListResponse;
import com.back.devc.domain.member.member.dto.AdmMemberStatusUpdateRequest;
import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.entity.MemberStatus;
import com.back.devc.domain.member.member.repository.MemberRepository;
import com.back.devc.domain.post.comment.repository.CommentRepository;
import com.back.devc.domain.post.post.repository.PostRepository;
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
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public Page<AdmMemberListResponse> getMembers(int page, int size, String keyword, MemberStatus status) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Member> members;

        // keyword + status 둘 다 없는 경우 → 전체 목록
        if ((keyword == null || keyword.isBlank()) && status == null) {
            members = memberRepository.findAll(pageable);
        }
        // keyword만 있는 경우
        else if (status == null) {
            members = memberRepository
                    .findByNicknameContainingOrEmailContaining(keyword, keyword, pageable);
        }
        // status만 있는 경우
        else if (keyword == null || keyword.isBlank()) {
            members = memberRepository.findByStatus(status, pageable);
        }
        // 둘 다 있는 경우
        else {
            members = memberRepository
                    .findByStatusAndNicknameContainingOrStatusAndEmailContaining(
                            status, keyword,
                            status, keyword,
                            pageable
                    );
        }

        return members.map(member -> {
            long postCount = postRepository.countByMember(member);
            long commentCount = commentRepository.countByUserIdAndIsDeletedFalse(member.getUserId());

            return AdmMemberListResponse.of(member, postCount, commentCount);
        });
    }

    // 2. 회원 상세 조회
    public AdmMemberDetailResponse getMemberDetail(Long userId) {
        Member member = findMemberOrThrow(userId);
        return convertToDetailResponse(member);
    }


    // 3. 회원 상태 변경
    @Transactional
    public AdmMemberDetailResponse updateMemberStatus(Long userId, AdmMemberStatusUpdateRequest request) {
        Member member = findMemberOrThrow(userId);
        member.updateStatus(request.status());
        return convertToDetailResponse(member);
    }

    /*
    *   내부에서 공통으로 사용할 메서드
    */
    // 조립 메서드
    private AdmMemberDetailResponse convertToDetailResponse(Member member) {
        long postCount = postRepository.countByMember(member);
        long commentCount = commentRepository.countByUserIdAndIsDeletedFalse(member.getUserId());
        return AdmMemberDetailResponse.of(member, postCount, commentCount);
    }
    //findById + 예외처리 메서드
    private Member findMemberOrThrow(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));
    }
}