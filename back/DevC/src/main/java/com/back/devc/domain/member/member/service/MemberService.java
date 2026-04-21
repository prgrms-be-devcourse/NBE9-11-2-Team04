package com.back.devc.domain.member.member.service;

import com.back.devc.domain.member.member.dto.MemberWithdrawResponse;
import com.back.devc.domain.member.member.dto.MyInfoResponse;
import com.back.devc.domain.member.member.dto.PublicProfilePostResponse;
import com.back.devc.domain.member.member.dto.PublicProfileResponse;
import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.repository.MemberRepository;
import com.back.devc.domain.post.post.repository.PostRepository;
import com.back.devc.global.exception.ApiException;
import com.back.devc.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

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

    @Transactional(readOnly = true)
    public PublicProfileResponse getPublicProfile(Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));

        List<PublicProfilePostResponse> posts = postRepository
                .findTop20ByMemberAndIsDeletedFalseOrderByCreatedAtDesc(member)
                .stream()
                .map(post -> new PublicProfilePostResponse(
                        post.getPostId(),
                        post.getTitle(),
                        post.getLikeCount(),
                        post.getCommentCount(),
                        post.getCreatedAt()
                ))
                .toList();

        return new PublicProfileResponse(
                member.getUserId(),
                member.getNickname(),
                posts
        );
    }

    @Transactional
    public MemberWithdrawResponse withdraw(Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));

        member.withdraw();
        return new MemberWithdrawResponse(member.getUserId());
    }
}
