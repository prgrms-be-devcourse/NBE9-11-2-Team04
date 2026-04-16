package com.back.devc.domain.post.post.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.post.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {


    Page<Post> findByIsDeletedFalse(Pageable pageable);
    Page<Post> findByCategoryCategoryIdAndIsDeletedFalse(Long categoryId, Pageable pageable);

    List<Post> findAllByOrderByCreatedAtDesc();
    List<Post> findAllByMember(Member member);

    List<Post> findByIsDeletedFalse();

    List<Post> findByIsDeletedFalseOrderByCreatedAtDesc();

    List<Post> findByIsDeletedFalseOrderByViewCountDesc();

    List<Post> findByIsDeletedFalseOrderByLikeCountDesc();

    List<Post> findByCategoryCategoryIdAndIsDeletedFalse(long categoryId);


    long countByMember(Member member);
}
