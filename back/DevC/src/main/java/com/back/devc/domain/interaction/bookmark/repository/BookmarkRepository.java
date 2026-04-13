package com.back.devc.domain.interaction.bookmark.repository;

import com.back.devc.domain.interaction.bookmark.entity.Bookmark;
import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.post.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Integer> {

    boolean existsByMemberAndPost(Member member, Post post);

    Optional<Bookmark> findByMemberAndPost(Member member, Post post);

    List<Bookmark> findAllByMember(Member member);
}