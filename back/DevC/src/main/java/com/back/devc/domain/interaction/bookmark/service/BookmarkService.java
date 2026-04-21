package com.back.devc.domain.interaction.bookmark.service;

import com.back.devc.domain.interaction.bookmark.dto.BookmarkResponse;
import com.back.devc.domain.interaction.bookmark.dto.BookmarkedPostResponse;
import com.back.devc.domain.interaction.bookmark.entity.Bookmark;
import com.back.devc.domain.interaction.bookmark.repository.BookmarkRepository;
import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.repository.MemberRepository;
import com.back.devc.domain.member.member.util.MemberDisplayUtil;
import com.back.devc.domain.post.post.entity.Post;
import com.back.devc.domain.post.post.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    @Transactional
    public BookmarkResponse createBookmark(Long userId, Long postId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다. id=" + userId));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. id=" + postId));

        if (bookmarkRepository.existsByMemberAndPost(member, post)) {
            return new BookmarkResponse(
                    post.getPostId(),
                    true,
                    "이미 북마크한 게시글입니다."
            );
        }

        Bookmark bookmark = new Bookmark(member, post);
        bookmarkRepository.save(bookmark);

        return new BookmarkResponse(
                post.getPostId(),
                true,
                "북마크가 추가되었습니다."
        );
    }

    @Transactional
    public BookmarkResponse cancelBookmark(Long userId, Long postId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다. id=" + userId));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. id=" + postId));

        Bookmark bookmark = bookmarkRepository.findByMemberAndPost(member, post)
                .orElse(null);

        if (bookmark == null) {
            return new BookmarkResponse(
                    post.getPostId(),
                    false,
                    "북마크가 이미 취소된 상태입니다."
            );
        }

        bookmarkRepository.delete(bookmark);

        return new BookmarkResponse(
                post.getPostId(),
                false,
                "북마크가 취소되었습니다."
        );
    }

    public List<BookmarkedPostResponse> getBookmarkedPosts(Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다. id=" + userId));

        List<Bookmark> bookmarks = bookmarkRepository.findAllByMember(member);

        return bookmarks.stream()
                .map(bookmark -> {
                    Post post = bookmark.getPost();
                    return new BookmarkedPostResponse(
                            post.getPostId(),
                            post.getTitle(),
                            MemberDisplayUtil.getDisplayName(post.getMember()),
                            post.getCategory().getCategoryId(),
                            post.getLikeCount(),
                            post.getCommentCount(),
                            post.getCreatedAt()
                    );
                })
                .toList();
    }
}