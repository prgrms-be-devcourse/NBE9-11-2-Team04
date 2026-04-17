package com.back.devc.domain.post.post.service;

import com.back.devc.domain.interaction.postLike.repository.PostLikeRepository;
import com.back.devc.domain.interaction.bookmark.repository.BookmarkRepository;
import com.back.devc.domain.post.post.dto.PostDetailResponse;

import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.repository.MemberRepository;
import com.back.devc.domain.post.category.entity.Category;
import com.back.devc.domain.post.category.repository.CategoryRepository;
import com.back.devc.domain.post.post.entity.Post;
import com.back.devc.domain.post.post.repository.PostRepository;
import com.back.devc.domain.post.post.type.PostSearchType;
import com.back.devc.domain.post.post.type.PostSortType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    // 게시글 상세 조회 시 현재 로그인 사용자의 좋아요 여부를 확인하기 위해 추가한 repository
    private final PostLikeRepository postLikeRepository;
    // 게시글 상세 조회 시 현재 로그인 사용자의 북마크 여부와 북마크 수를 확인하기 위해 추가한 repository
    private final BookmarkRepository bookmarkRepository;

    // 게시글 작성
    public Post write(Long userId, Long categoryId, String title, String content) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다. id=" + userId));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다. id=" + categoryId));

        Post post = Post.builder()
                .member(member)
                .category(category)
                .title(title)
                .content(content)
                .build();

        return postRepository.save(post);
    }

    // 단건 조회
    @Transactional(readOnly = true)
    public Post findById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. id=" + postId));
    }

    // 단건 상세 조회 (현재 로그인 사용자의 좋아요 여부 포함)
    // 게시글이 현재 isDeleted=false인 경우만 상세 조회 할 수 있도록 변경하였습니다.

    /**
     * 게시글 단건 상세 조회
     *
     * 기존 게시글 상세 정보에 더해,
     * 현재 로그인한 사용자가 이 게시글에 좋아요를 눌렀는지도 함께 계산해서 내려줌
     *
     * 이렇게 하면 프론트가 게시글 상세 페이지에 진입했을 때
     * 서버 기준의 실제 좋아요 상태를 바로 표시할 수 있음
     */

    @Transactional(readOnly = true)
    public PostDetailResponse findDetailById(Long postId, Long loginUserId) {
        Post post = postRepository.findByPostIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. id=" + postId));

        // 비로그인 사용자는 좋아요 여부를 확인할 수 없으므로 false 처리
        // 로그인 사용자라면 post_likes 테이블을 조회해서 현재 게시글에 좋아요를 눌렀는지 확인
        boolean liked = loginUserId != null
                && postLikeRepository.existsByMember_UserIdAndPost_PostId(loginUserId, postId);

        // 비로그인 사용자는 북마크 여부를 확인할 수 없으므로 false 처리
        // 로그인 사용자라면 bookmarks 테이블을 조회해서 현재 게시글을 북마크했는지 확인
        boolean bookmarked = loginUserId != null
                && bookmarkRepository.existsByMember_UserIdAndPost_PostId(loginUserId, postId);

        // 상세 페이지에서 북마크 수를 바로 표시할 수 있도록 현재 게시글의 북마크 총 개수도 함께 계산
        int bookmarkCount = Math.toIntExact(bookmarkRepository.countByPost_PostId(postId));

        // 게시글 기본 정보와 현재 로그인 사용자의 좋아요/북마크 여부, 북마크 수를 함께 응답 DTO로 변환
        return PostDetailResponse.from(post, liked, bookmarked, bookmarkCount);
    }


    // 게시글 목록 조회 (페이징 + 정렬 + 카테고리 필터 + 검색기능 포함)
    @Transactional(readOnly = true)
    public Page<Post> getPosts(Long categoryId, String keyword, PostSearchType searchType, PostSortType sort, int page, int size) {

        Sort jpaSort = switch (sort) {
            case VIEWS -> Sort.by(
                    Sort.Order.desc("viewCount"),
                    Sort.Order.desc("createdAt")
            );
            case LIKES -> Sort.by(
                    Sort.Order.desc("likeCount"),
                    Sort.Order.desc("createdAt")
            );
            default -> Sort.by(Sort.Order.desc("createdAt"));
        };

        Pageable pageable = PageRequest.of(page, size, jpaSort);

        boolean hasKeyword = keyword != null && !keyword.isBlank();

        if (hasKeyword) {

            String kw = keyword.trim(); // 빈칸 제거

            // 제목 검색
            if (searchType == PostSearchType.TITLE) {
                return (categoryId != null)
                        ? postRepository.findByCategoryCategoryIdAndTitleContainingAndIsDeletedFalse(categoryId, kw, pageable)
                        : postRepository.findByTitleContainingAndIsDeletedFalse(kw, pageable);
            }

            // 내용 검색
            if (searchType == PostSearchType.CONTENT) {
                return (categoryId != null)
                        ? postRepository.findByCategoryCategoryIdAndContentContainingAndIsDeletedFalse(categoryId, kw, pageable)
                        : postRepository.findByContentContainingAndIsDeletedFalse(kw, pageable);
            }

            // 제목 + 내용 검색
            return (categoryId != null)
                    ? postRepository.findByCategoryCategoryIdAndTitleContainingOrContentContainingAndIsDeletedFalse(
                    categoryId, kw, kw, pageable
            )
                    : postRepository.findByTitleContainingOrContentContainingAndIsDeletedFalse(
                    kw, kw, pageable
            );
        }


        if (categoryId != null) {
            if (!categoryRepository.existsById(categoryId)) {
                throw new EntityNotFoundException("존재하지 않는 카테고리입니다.");
            }
            return postRepository.findByCategoryCategoryIdAndIsDeletedFalse(categoryId, pageable);
        }

        return postRepository.findByIsDeletedFalse(pageable);
    }

    // 조회수 증가
    @Transactional
    public void increaseViewCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 존재하지 않습니다."));

        post.increaseViewCount();
    }

    // 댓글 수 증가
    @Transactional
    public void increaseCommentCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 존재하지 않습니다."));

        post.increaseCommentCount();
    }

    // 수정
    public Post update(Long memberId, Long postId, String title, String content, Long categoryId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

        if (!post.getMember().getUserId().equals(memberId)) {
            throw new RuntimeException("수정 권한이 없습니다.");
        }

        if (post.isDeleted()) {
            throw new EntityNotFoundException("삭제된 게시글입니다.");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다."));

        post.update(title, content, category);
        return post;
    }

    // 삭제 (soft delete)
    public void delete(Long memberId, Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

        if (!post.getMember().getUserId().equals(memberId)) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }

        if (post.isDeleted()) {
            throw new EntityNotFoundException("삭제된 게시글입니다.");
        }

        post.delete();
    }
}