package com.back.devc.domain.interaction.bookmark.service;

import com.back.devc.domain.interaction.bookmark.dto.BookmarkResponse;
import com.back.devc.domain.interaction.bookmark.dto.BookmarkedPostResponse;
import com.back.devc.domain.interaction.bookmark.entity.Bookmark;
import com.back.devc.domain.interaction.bookmark.repository.BookmarkRepository;
import com.back.devc.domain.interaction.notification.service.NotificationService;
import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.repository.MemberRepository;
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
    private final NotificationService notificationService;

    /**
     * 북마크 생성
     *
     * - 사용자와 게시글 존재 여부를 먼저 검증한다.
     * - 이미 북마크한 게시글인 경우 예외 대신 기존 응답 형식을 유지한다.
     * - 북마크 저장 이후 알림을 생성한다.
     */
    @Transactional
    public BookmarkResponse createBookmark(Long userId, Long postId) {
        Member member = findMemberById(userId);
        Post post = findPostById(postId);

        // 기존 팀 동작을 유지하기 위해 중복 북마크도 예외가 아닌 응답 DTO로 반환한다.
        if (bookmarkRepository.existsByMemberAndPost(member, post)) {
            return BookmarkResponse.alreadyBookmarked(post.getPostId());
        }

        Bookmark bookmark = new Bookmark(member, post);
        bookmarkRepository.save(bookmark);

        // 북마크 저장이 완료된 이후 알림을 생성한다.
        notificationService.createBookmarkNotification(postId, userId);

        return BookmarkResponse.created(post.getPostId());
    }

    /**
     * 북마크 취소
     *
     * - 사용자의 북마크 여부를 조회한 뒤 존재하면 삭제한다.
     * - 이미 삭제된 상태인 경우에도 예외 대신 응답 DTO를 반환한다.
     */
    @Transactional
    public BookmarkResponse cancelBookmark(Long userId, Long postId) {
        Member member = findMemberById(userId);
        Post post = findPostById(postId);

        Bookmark bookmark = bookmarkRepository.findByMemberAndPost(member, post)
                .orElse(null);

        // 기존 동작 기준으로, 이미 취소된 상태도 정상 응답으로 처리한다.
        if (bookmark == null) {
            return BookmarkResponse.alreadyCanceled(post.getPostId());
        }

        bookmarkRepository.delete(bookmark);

        return BookmarkResponse.canceled(post.getPostId());
    }

    /**
     * 사용자가 북마크한 게시글 목록 조회
     *
     * - 북마크 엔티티 목록을 조회한 뒤, 응답 전용 DTO로 변환한다.
     * - 컨트롤러에는 엔티티를 직접 노출하지 않는다.
     */
    public List<BookmarkedPostResponse> getBookmarkedPosts(Long userId) {
        Member member = findMemberById(userId);
        List<Bookmark> bookmarks = bookmarkRepository.findAllByMember(member);

        return bookmarks.stream()
                .map(Bookmark::getPost)
                .map(BookmarkedPostResponse::from)
                .toList();
    }

    /**
     * 회원 조회 공통 메서드
     *
     * - 서비스 전반에서 동일한 회원 조회 로직을 재사용하기 위해 분리한다.
     * - 조회 실패 시 일관된 예외 메시지를 사용한다.
     */
    private Member findMemberById(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다. id=" + userId));
    }

    /**
     * 게시글 조회 공통 메서드
     *
     * - 서비스 전반에서 동일한 게시글 조회 로직을 재사용하기 위해 분리한다.
     * - 조회 실패 시 일관된 예외 메시지를 사용한다.
     */
    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. id=" + postId));
    }

}