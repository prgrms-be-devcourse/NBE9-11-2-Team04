package com.back.devc.domain.post.post.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.back.devc.domain.post.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByIsDeletedFalse(Pageable pageable);

    Page<Post> findByCategoryCategoryIdAndIsDeletedFalse(Long categoryId, Pageable pageable);
}