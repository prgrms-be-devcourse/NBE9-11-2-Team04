package com.back.devc.domain.post.post.repository;

import com.back.devc.domain.post.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    //게시물 정렬 관련해서 추가 구현 예정

    // 삭제 안된 게시글 조회
    List<Post> findByIsDeletedFalse();

    // 최신순
    List<Post> findByIsDeletedFalseOrderByCreatedAtDesc();

    // 조회수순
    List<Post> findByIsDeletedFalseOrderByViewCountDesc();

    // 좋아요순
    List<Post> findByIsDeletedFalseOrderByLikeCountDesc();

    // 카테고리별 조회
    List<Post> findByCategoryCategoryIdAndIsDeletedFalse(long categoryId);

}
