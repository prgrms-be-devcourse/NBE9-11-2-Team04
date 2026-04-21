package com.back.devc.domain.interaction.bookmark.code;

import com.back.devc.global.response.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 북마크 도메인 성공 코드
 *
 * - 네이밍 규칙: [DOMAIN]_[HTTP]_[ACTION]
 */
@Getter
@RequiredArgsConstructor
public enum BookmarkSuccessCode implements SuccessCode {

    BOOKMARK_201_CREATE(HttpStatus.CREATED, "북마크가 추가되었습니다."),
    BOOKMARK_200_DELETE(HttpStatus.OK, "북마크가 취소되었습니다.");

    private final HttpStatus status;
    private final String message;
}
