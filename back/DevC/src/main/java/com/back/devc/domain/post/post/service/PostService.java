package com.back.devc.domain.post.post.service;

import com.back.devc.domain.interaction.bookmark.repository.BookmarkRepository;
import com.back.devc.domain.interaction.postLike.repository.PostLikeRepository;
import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.repository.MemberRepository;
import com.back.devc.domain.member.searchLog.dto.CreateSearchLogRequest;
import com.back.devc.domain.member.searchLog.service.SearchLogService;
import com.back.devc.domain.post.category.entity.Category;
import com.back.devc.domain.post.category.repository.CategoryRepository;
import com.back.devc.domain.post.post.dto.*;
import com.back.devc.domain.post.post.entity.Post;
import com.back.devc.domain.post.post.repository.PostRepository;
import com.back.devc.domain.post.post.type.PostSearchType;
import com.back.devc.domain.post.post.type.PostSortType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final PostLikeRepository postLikeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final SearchLogService searchLogService;

    public PostCreateResponse write(Long userId, PostCreateRequest request) {

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다."));

        Post post = Post.builder()
                .member(member)
                .category(category)
                .title(request.title())
                .content(request.content())
                .build();

        Post saved = postRepository.save(post);

        return PostCreateResponse.from(saved);
    }


    @Transactional
    public PostDetailResponse findDetailById(Long postId, Long loginUserId) {
        Post post = postRepository.findByPostIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. id=" + postId));
        post.increaseViewCount();
        boolean liked = loginUserId != null
                && postLikeRepository.existsByMember_UserIdAndPost_PostId(loginUserId, postId);

        boolean bookmarked = loginUserId != null
                && bookmarkRepository.existsByMember_UserIdAndPost_PostId(loginUserId, postId);

        int bookmarkCount = Math.toIntExact(bookmarkRepository.countByPost_PostId(postId));

        return PostDetailResponse.from(post, liked, bookmarked, bookmarkCount);
    }

    @Transactional
    public Page<PostListResponse> getPosts(
            Long loginUserId,
            Long categoryId,
            String keyword,
            PostSearchType searchType,
            PostSortType sort,
            int page,
            int size
    ) {
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

        if (loginUserId != null && hasKeyword) {
            searchLogService.createSearchLog(
                    loginUserId,
                    new CreateSearchLogRequest(keyword.trim())
            );
        }

        Page<Post> result;

        if (hasKeyword) {
            String kw = keyword.trim();

            if (searchType == PostSearchType.TITLE) {
                result = (categoryId != null)
                        ? postRepository.findByCategoryCategoryIdAndTitleContainingAndIsDeletedFalse(categoryId, kw, pageable)
                        : postRepository.findByTitleContainingAndIsDeletedFalse(kw, pageable);
            } else if (searchType == PostSearchType.CONTENT) {
                result = (categoryId != null)
                        ? postRepository.findByCategoryCategoryIdAndContentContainingAndIsDeletedFalse(categoryId, kw, pageable)
                        : postRepository.findByContentContainingAndIsDeletedFalse(kw, pageable);
            } else {
                result = (categoryId != null)
                        ? postRepository.searchPosts(categoryId, kw, pageable)
                        : postRepository.searchByKeyword(kw, pageable);
            }
        } else {
            if (categoryId != null) {
                if (!categoryRepository.existsById(categoryId)) {
                    throw new EntityNotFoundException("존재하지 않는 카테고리입니다.");
                }
                result = postRepository.findByCategoryCategoryIdAndIsDeletedFalse(categoryId, pageable);
            } else {
                result = postRepository.findByIsDeletedFalse(pageable);
            }
        }

        return result.map(post -> {
            boolean liked = loginUserId != null
                    && postLikeRepository.existsByMember_UserIdAndPost_PostId(loginUserId, post.getPostId());

            boolean bookmarked = loginUserId != null
                    && bookmarkRepository.existsByMember_UserIdAndPost_PostId(loginUserId, post.getPostId());

            return PostListResponse.from(post, liked, bookmarked);
        });
    }

    @Transactional
    public void increaseViewCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 존재하지 않습니다."));

        post.increaseViewCount();
    }

    @Transactional
    public void increaseCommentCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 존재하지 않습니다."));

        post.increaseCommentCount();
    }

    public PostUpdateResponse update(Long memberId, Long postId, PostUpdateRequest request) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

        if (!post.getMember().getUserId().equals(memberId)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        if (post.isDeleted()) {
            throw new EntityNotFoundException("삭제된 게시글입니다.");
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다."));

        post.update(
                request.title(),
                request.content(),
                category
        );

        return PostUpdateResponse.from(post);
    }

    public PostDeleteResponse delete(Long memberId, Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

        if (!post.getMember().getUserId().equals(memberId)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        if (post.isDeleted()) {
            throw new EntityNotFoundException("이미 삭제된 게시글입니다.");
        }

        post.delete();

        return PostDeleteResponse.of(postId);
    }
}