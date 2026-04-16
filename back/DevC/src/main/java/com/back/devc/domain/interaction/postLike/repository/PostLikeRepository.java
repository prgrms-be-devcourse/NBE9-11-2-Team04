package com.back.devc.domain.interaction.postLike.repository;

import com.back.devc.domain.interaction.postLike.entity.PostLike;
import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.post.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByMemberAndPost(Member member, Post post);

    Optional<PostLike> findByMemberAndPost(Member member, Post post);

    List<PostLike> findAllByMember(Member member);

    boolean existsByMember_UserIdAndPost_PostId(Long userId, Long postId);
}