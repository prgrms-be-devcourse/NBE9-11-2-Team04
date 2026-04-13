package com.back.devc.domain.post.post.entity;

import com.back.devc.domain.member.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
public class Post {

    @Id
    private Long postId;

    private String title;

    private long likeCount;

    private long commentCount;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Member member;

    protected Post() {}

    public Post(Long postId, String title, Member member) {
        this.postId = postId;
        this.title = title;
        this.member = member;
        this.likeCount = 0;
        this.commentCount = 0;
        this.createdAt = LocalDateTime.now();
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
}