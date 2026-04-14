package com.back.devc.domain.post.post.repository;

import com.back.devc.domain.post.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    //게시물 정렬 관련해서 추가 구현 예정
}
