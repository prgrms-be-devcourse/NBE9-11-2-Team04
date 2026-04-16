package com.back.devc.domain.interaction.postLike.service;

import com.back.devc.domain.interaction.postLike.dto.LikedPostResponse;
import com.back.devc.domain.interaction.postLike.dto.PostLikeResponse;
import com.back.devc.domain.interaction.postLike.entity.PostLike;
import com.back.devc.domain.interaction.postLike.repository.PostLikeRepository;
import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.repository.MemberRepository;
import com.back.devc.domain.post.post.entity.Post;
import com.back.devc.domain.post.post.repository.PostRepository;
import com.back.devc.domain.interaction.notification.service.NotificationService;
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
            throw new IllegalStateException("이미 좋아요를 누른 게시글입니다.");
        }

        PostLike postLike = new PostLike(member, post);
        postLikeRepository.save(postLike);

        post.increaseLikeCount();
        notificationService.createPostLikeNotification(postId, userId);

        return new PostLikeResponse(post.getPostId(), true, post.getLikeCount());
    }

    @Transactional
    public PostLikeResponse cancelLike(Long userId, Long postId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다. id=" + userId));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. id=" + postId));

        PostLike postLike = postLikeRepository.findByMemberAndPost(member, post)
                .orElseThrow(() -> new EntityNotFoundException("좋아요가 존재하지 않습니다."));

        postLikeRepository.delete(postLike);
        post.decreaseLikeCount();

        return new PostLikeResponse(post.getPostId(), false, post.getLikeCount());
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
                            post.getMember().getNickname(),
                            post.getLikeCount(),
                            post.getCommentCount(),
                            post.getCreatedAt()
                    );
                })
                .toList();
    }
}