package com.back.devc.domain.member.mypage.service;

import com.back.devc.domain.interaction.bookmark.dto.BookmarkedPostResponse;
import com.back.devc.domain.interaction.bookmark.service.BookmarkService;
import com.back.devc.domain.interaction.postLike.dto.LikedPostResponse;
import com.back.devc.domain.interaction.postLike.service.PostLikeService;
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
    private final PostLikeService postLikeService;
    private final BookmarkService bookmarkService;

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

    public List<LikedPostResponse> getMyLikedPosts(Long userId) {
        memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다. id=" + userId));

        return postLikeService.getLikedPosts(userId);
    }

    public List<BookmarkedPostResponse> getMyBookmarkedPosts(Long userId) {
        memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다. id=" + userId));

        return bookmarkService.getBookmarkedPosts(userId);
    }

    @Transactional
    public MyProfileResponse updateMyProfile(Long userId, UpdateMyProfileRequest request) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다. id=" + userId));

        String newEmail = request.email().trim();
        String newNickname = request.nickname().trim();

        if (!member.getEmail().equals(newEmail) && memberRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        if (!member.getNickname().equals(newNickname) && memberRepository.existsByNickname(newNickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        member.updateEmail(newEmail);
        member.updateNickname(newNickname);

        return new MyProfileResponse(
                member.getUserId(),
                member.getEmail(),
                member.getNickname()
        );
    }
}