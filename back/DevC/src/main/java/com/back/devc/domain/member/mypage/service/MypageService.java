package com.back.devc.domain.member.mypage.service;

import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.repository.MemberRepository;
import com.back.devc.domain.member.mypage.dto.MyCommentResponse;
import com.back.devc.domain.member.mypage.dto.MyPostResponse;
import com.back.devc.domain.member.mypage.dto.MyProfileResponse;
import com.back.devc.domain.member.mypage.dto.UpdateMyProfileRequest;
import com.back.devc.domain.post.comment.entity.Comment;
import com.back.devc.domain.post.comment.repository.CommentRepository;
import com.back.devc.domain.post.post.entity.Post;
import com.back.devc.domain.post.post.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MypageService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public MyProfileResponse getMyProfile(Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다. id=" + userId));

        return new MyProfileResponse(
                member.getUserId(),
                member.getEmail(),
                member.getNickname()
        );
    }

    public List<MyPostResponse> getMyPosts(Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다. id=" + userId));

        List<Post> posts = postRepository.findAllByMember(member);

        return posts.stream()
                .map(post -> new MyPostResponse(
                        post.getPostId(),
                        post.getTitle(),
                        post.getLikeCount(),
                        post.getCommentCount(),
                        post.getCreatedAt()
                ))
                .toList();
    }

    public List<MyCommentResponse> getMyComments(Long userId) {
        memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다. id=" + userId));

        List<Comment> comments = commentRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId);

        return comments.stream()
                .map(comment -> new MyCommentResponse(
                        comment.getId(),
                        comment.getPostId(),
                        comment.getContent(),
                        comment.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public MyProfileResponse updateMyProfile(Long userId, UpdateMyProfileRequest request) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다. id=" + userId));

        member.updateNickname(request.nickname());

        return new MyProfileResponse(
                member.getUserId(),
                member.getEmail(),
                member.getNickname()
        );
    }
}