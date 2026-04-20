package com.back.devc.domain.post.post.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.post.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {


    Page<Post> findByIsDeletedFalse(Pageable pageable);
    Page<Post> findByCategoryCategoryIdAndIsDeletedFalse(Long categoryId, Pageable pageable);

    Page<Post> findByTitleContainingAndIsDeletedFalse(String title, Pageable pageable);
    Page<Post> findByContentContainingAndIsDeletedFalse(String content, Pageable pageable);
    Page<Post> findByTitleContainingOrContentContainingAndIsDeletedFalse(
            String title,
            String content,
            Pageable pageable
    );


    Page<Post> findByCategoryCategoryIdAndTitleContainingAndIsDeletedFalse(
            Long categoryId, String title, Pageable pageable
    );

    Page<Post> findByCategoryCategoryIdAndContentContainingAndIsDeletedFalse(
            Long categoryId, String content, Pageable pageable
    );

    Page<Post> findByCategoryCategoryIdAndTitleContainingOrContentContainingAndIsDeletedFalse(
            Long categoryId, String title, String content, Pageable pageable
    );

    List<Post> findAllByOrderByCreatedAtDesc();
    List<Post> findAllByMember(Member member);
    List<Post> findTop20ByMemberAndIsDeletedFalseOrderByCreatedAtDesc(Member member);

    Optional<Post> findByPostIdAndIsDeletedFalse(Long id);
    List<Post> findByIsDeletedFalse();

    List<Post> findByIsDeletedFalseOrderByCreatedAtDesc();

    List<Post> findByIsDeletedFalseOrderByViewCountDesc();

    List<Post> findByIsDeletedFalseOrderByLikeCountDesc();

    List<Post> findByCategoryCategoryIdAndIsDeletedFalse(long categoryId);


    long countByMember(Member member);
}
