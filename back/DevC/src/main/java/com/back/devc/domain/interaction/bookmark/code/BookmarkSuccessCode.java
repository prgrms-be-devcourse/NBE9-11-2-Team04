package com.back.devc.domain.interaction.bookmark.code;

import com.back.devc.global.response.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 북마크 도메인 성공 코드
 *
 * 네이밍 규칙: [DOMAIN]_[HTTP]_[ACTION]
 */
@Getter
@RequiredArgsConstructor
public enum BookmarkSuccessCode implements SuccessCode {

    BOOKMARK_201_CREATE(HttpStatus.CREATED, "BOOKMARK_201_CREATE", "북마크가 추가되었습니다."),
    BOOKMARK_200_DELETE(HttpStatus.OK, "BOOKMARK_200_DELETE", "북마크가 취소되었습니다."),
    BOOKMARK_200_READ_LIST(HttpStatus.OK, "BOOKMARK_200_READ_LIST", "북마크한 게시글 목록 조회 성공");

    private final HttpStatus status;
    private final String code;
    private final String message;
}