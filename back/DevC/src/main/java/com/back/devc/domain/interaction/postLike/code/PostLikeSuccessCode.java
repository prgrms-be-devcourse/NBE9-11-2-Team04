package com.back.devc.domain.interaction.postLike.code;

import com.back.devc.global.response.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 좋아요 도메인 성공 코드
 *
 * - 네이밍 규칙: [DOMAIN]_[HTTP]_[ACTION]
 */
@Getter
@RequiredArgsConstructor
public enum PostLikeSuccessCode implements SuccessCode {

    POSTLIKE_201_CREATE(HttpStatus.CREATED, "좋아요가 추가되었습니다."),
    POSTLIKE_200_DELETE(HttpStatus.OK, "좋아요가 취소되었습니다."),
    POSTLIKE_200_READ_LIST(HttpStatus.OK, "좋아요한 게시글 목록 조회에 성공했습니다.");

    private final HttpStatus status;
    private final String message;
}