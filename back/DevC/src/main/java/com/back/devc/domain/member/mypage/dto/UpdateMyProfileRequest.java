package com.back.devc.domain.member.mypage.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateMyProfileRequest(
        @NotBlank(message = "닉네임은 필수입니다.")
        String nickname
) {
}