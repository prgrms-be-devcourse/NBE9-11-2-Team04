package com.back.devc.domain.member.mypage.service;

import com.back.devc.domain.interaction.bookmark.dto.BookmarkedPostResponse;
import com.back.devc.domain.interaction.bookmark.service.BookmarkService;
import com.back.devc.domain.interaction.postLike.dto.LikedPostResponse;
import com.back.devc.domain.interaction.postLike.dto.LikedPostsQuery;
import com.back.devc.domain.interaction.postLike.service.PostLikeService;
import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.repository.MemberRepository;
import com.back.devc.domain.member.mypage.dto.*;
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
    private final PostLikeService postLikeService;
    private final BookmarkService bookmarkService;

    // ✅ 공통 메서드
    private Member getMember(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다. id=" + userId));
    }

    public MyProfileResponse getMyProfile(Long userId) {
        Member member = getMember(userId);

        return new MyProfileResponse(
                member.getUserId(),
                member.getEmail(),
                member.getNickname()
        );
    }

    public List<MyPostResponse> getMyPosts(Long userId) {
        Member member = getMember(userId);

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

    // ✅ N+1 제거 (Repository에서 DTO 바로 조회)
    public List<MyCommentResponse> getMyComments(Long userId) {
        getMember(userId);
        return commentRepository.findMyComments(userId);
    }

    public List<LikedPostResponse> getMyLikedPosts(Long userId) {
        getMember(userId);

        LikedPostsQuery query = LikedPostsQuery.builder()
                .userId(userId)
                .build();

        return postLikeService.getLikedPosts(query);
    }

    public List<BookmarkedPostResponse> getMyBookmarkedPosts(Long userId) {
        getMember(userId);
        return bookmarkService.getBookmarkedPosts(userId);
    }

    @Transactional
    public MyProfileResponse updateMyProfile(Long userId, UpdateMyProfileRequest request) {
        Member member = getMember(userId);

        String newNickname = request.nickname().trim();

        if (!member.getNickname().equals(newNickname)
                && memberRepository.existsByNickname(newNickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        member.updateNickname(newNickname);

        return new MyProfileResponse(
                member.getUserId(),
                member.getEmail(),
                member.getNickname()
        );
    }
}