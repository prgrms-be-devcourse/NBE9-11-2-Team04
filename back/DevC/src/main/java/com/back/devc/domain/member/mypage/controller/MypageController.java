package com.back.devc.domain.member.mypage.controller;

import com.back.devc.domain.member.mypage.dto.MyCommentResponse;
import com.back.devc.domain.member.mypage.dto.MyPostResponse;
import com.back.devc.domain.member.mypage.dto.MyProfileResponse;
import com.back.devc.domain.member.mypage.dto.UpdateMyProfileRequest;
import com.back.devc.domain.member.mypage.service.MypageService;
import com.back.devc.global.security.jwt.JwtPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MypageController {

    private final MypageService mypageService;

    // 내 프로필 조회
    @GetMapping
    public MyProfileResponse getMyProfile(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        return mypageService.getMyProfile(principal.userId());
    }

    // 내가 쓴 글
    @GetMapping("/posts")
    public List<MyPostResponse> getMyPosts(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        return mypageService.getMyPosts(principal.userId());
    }

    // 내가 쓴 댓글
    @GetMapping("/comments")
    public List<MyCommentResponse> getMyComments(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        return mypageService.getMyComments(principal.userId());
    }

    // 프로필 수정
    @PatchMapping
    public MyProfileResponse updateMyProfile(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestBody @Valid UpdateMyProfileRequest request
    ) {
        return mypageService.updateMyProfile(principal.userId(), request);
    }
}