package com.back.devc.domain.member.mypage.controller;

import com.back.devc.domain.member.mypage.dto.MyPostResponse;
import com.back.devc.domain.member.mypage.dto.MyProfileResponse;
import com.back.devc.domain.member.mypage.service.MypageService;
import com.back.devc.global.security.jwt.JwtPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MypageController {

    private final MypageService mypageService;

    @GetMapping("/users/me")
    public MyProfileResponse getMyProfile(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        return mypageService.getMyProfile(principal.userId());
    }

    @GetMapping("/users/me/posts")
    public List<MyPostResponse> getMyPosts(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        return mypageService.getMyPosts(principal.userId());
    }
}