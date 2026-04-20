package com.back.devc.domain.interaction.postLike.service;

import com.back.devc.domain.interaction.notification.service.NotificationService;
import com.back.devc.domain.interaction.postLike.dto.LikedPostResponse;
import com.back.devc.domain.interaction.postLike.dto.PostLikeResponse;
import com.back.devc.domain.interaction.postLike.entity.PostLike;
import com.back.devc.domain.interaction.postLike.repository.PostLikeRepository;
import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.repository.MemberRepository;
import com.back.devc.domain.member.member.util.MemberDisplayUtil;
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
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    @Transactional
    public PostLikeResponse createLike(Long userId, Long postId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다. id=" + userId));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. id=" + postId));

        if (postLikeRepository.existsByMemberAndPost(member, post)) {
            return new PostLikeResponse(
                    post.getPostId(),
                    true,
                    post.getLikeCount(),
                    "이미 좋아요한 게시글입니다."
            );
        }

        PostLike postLike = new PostLike(member, post);
        postLikeRepository.save(postLike);

        post.increaseLikeCount();
        notificationService.createPostLikeNotification(postId, userId);

        return new PostLikeResponse(
                post.getPostId(),
                true,
                post.getLikeCount(),
                "좋아요가 추가되었습니다."
        );
    }

    @Transactional
    public PostLikeResponse cancelLike(Long userId, Long postId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다. id=" + userId));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. id=" + postId));

        PostLike postLike = postLikeRepository.findByMemberAndPost(member, post)
                .orElse(null);

        if (postLike == null) {
            return new PostLikeResponse(
                    post.getPostId(),
                    false,
                    post.getLikeCount(),
                    "좋아요가 이미 취소된 상태입니다."
            );
        }

        postLikeRepository.delete(postLike);
        post.decreaseLikeCount();

        return new PostLikeResponse(
                post.getPostId(),
                false,
                post.getLikeCount(),
                "좋아요가 취소되었습니다."
        );
    }

    public List<LikedPostResponse> getLikedPosts(Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다. id=" + userId));

        List<PostLike> postLikes = postLikeRepository.findAllByMember(member);

        return postLikes.stream()
                .map(postLike -> {
                    Post post = postLike.getPost();
                    return new LikedPostResponse(
                            post.getPostId(),
                            post.getTitle(),
                            MemberDisplayUtil.getDisplayName(post.getMember()),
                            post.getLikeCount(),
                            post.getCommentCount(),
                            post.getCreatedAt()
                    );
                })
                .toList();
    }
}