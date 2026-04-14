package com.back.devc.domain.post.post.entity;

import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.post.category.entity.Category;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    // 멤버 1 : 게시물 N
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Member member; // 현재 멤버 엔티티 없어서 발생하는 오류

    // 카테고리 1 : 게시물 N
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    //제목 길이 100자 제한
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    private int viewCount;
    private int likeCount;
    private int commentCount;

    private boolean isDeleted;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;


    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;


    public Post(Member member, Category category, String title, String content) {
        this.member = member;
        this.category = category;
        this.title = title;
        this.content = content;
        this.viewCount = 0;
        this.likeCount = 0;
        this.commentCount = 0;
        this.isDeleted = false;
    }



    // 조회수 증가
    public void increaseViewCount() {
        this.viewCount++;
    }

    // 댓글 수 증가
    public void increaseCommentCount() {
        this.commentCount++;
    }

    // 댓글 수 감소
    public void decreaseCommentCount() {
        this.commentCount--;
    }

    // 수정
    public void update(String title, String content, Category category) {
        this.title = title;
        this.content = content;
        this.category = category;
    }

    // 삭제 (soft delete)
    public void delete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}