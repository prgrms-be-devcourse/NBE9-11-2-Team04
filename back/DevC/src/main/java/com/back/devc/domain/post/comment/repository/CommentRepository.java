package com.back.devc.domain.post.comment.repository;

import com.back.devc.domain.post.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 게시글 기준 댓글 조회
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);

    // 내가 쓴 댓글 조회
    List<Comment> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId);
}