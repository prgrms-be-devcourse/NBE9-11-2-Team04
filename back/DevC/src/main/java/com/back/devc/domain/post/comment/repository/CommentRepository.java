package com.back.devc.domain.post.comment.repository;

import com.back.devc.domain.member.mypage.dto.MyCommentResponse;
import com.back.devc.domain.post.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 게시글 기준 댓글 조회
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);

    // 내가 쓴 댓글 조회
    List<Comment> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndIsDeletedFalse(Long userId);

    @Query("""
        SELECT new com.back.devc.domain.member.mypage.dto.MyCommentResponse(
            c.id,
            c.postId,
            p.title,
            c.content,
            c.createdAt
        )
        FROM Comment c
        JOIN Post p ON c.postId = p.postId
        WHERE c.userId = :userId
          AND c.isDeleted = false
          AND p.isDeleted = false
        ORDER BY c.createdAt DESC
    """)
    List<MyCommentResponse> findMyComments(Long userId);
}