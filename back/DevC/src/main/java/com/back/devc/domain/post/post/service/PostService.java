package com.back.devc.domain.post.post.service;

import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.post.category.entity.Category;
import com.back.devc.domain.post.category.repository.CategoryRepository;
import com.back.devc.domain.post.post.entity.Post;
import com.back.devc.domain.post.post.repository.PostRepository;
import com.back.devc.domain.post.post.type.PostSortType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;

    // 게시글 생성
    @Transactional
    public Post write(Member member, Long categoryId, String title, String content) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리가 존재하지 않습니다."));

        Post post = new Post(member, category, title, content);

        return postRepository.save(post);
    }

    // 게시글 수정
    @Transactional
    public Post update(Long postId, String title, String content, Long categoryId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리가 존재하지 않습니다."));

        post.update(title, content, category);

        return post;
    }

    // 게시글 삭제 (soft delete)
    @Transactional
    public void delete(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        //db상에는 존재하지만 isDeleted 상태가 true인 경우 처리
        if (post.isDeleted()) {
            throw new IllegalArgumentException("게시글이 존재하지 않습니다.");
        }

        post.delete();
    }

    // 게시글 단건 조회
    @Transactional(readOnly = true)
    public Post findById(Long postId) {

        return postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
    }

    // 게시글 전체 조회
    @Transactional(readOnly = true)
    public List<Post> findAll() {
        return postRepository.findAll();
    }

    // 조회수 증가
    @Transactional
    public void increaseViewCount(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        post.increaseViewCount();
    }

    //sort 종류에 따라서 (조회수순, 좋아요순, 최신순)
    @Transactional(readOnly = true)
    public Page<Post> getPosts(PostSortType sort, int page, int size) {

        Sort jpaSort = switch (sort) {
            case views -> Sort.by("viewCount").descending();
            case likes -> Sort.by("likeCount").descending();
            default -> Sort.by("createdAt").descending();
        };

        Pageable pageable = PageRequest.of(page, size, jpaSort);
        return postRepository.findByIsDeletedFalse(pageable);
    }


    // 댓글 수 증가
    @Transactional
    public void increaseCommentCount(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        post.increaseCommentCount();
    }

    // 댓글 수 감소
    @Transactional
    public void decreaseCommentCount(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        post.decreaseCommentCount();
    }
}