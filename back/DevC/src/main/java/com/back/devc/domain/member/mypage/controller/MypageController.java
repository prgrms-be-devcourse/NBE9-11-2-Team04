package com.back.devc.domain.member.mypage.controller;

import com.back.devc.domain.member.mypage.dto.MyPostResponse;
import com.back.devc.domain.member.mypage.dto.MyProfileResponse;
import com.back.devc.domain.member.mypage.service.MypageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MypageController {

    private final MypageService mypageService;

    @GetMapping("/users/me")
    public MyProfileResponse getMyProfile(@RequestParam Long userId) {
        return mypageService.getMyProfile(userId);
    }

    @GetMapping("/users/me/posts")
    public List<MyPostResponse> getMyPosts(@RequestParam Long userId) {
        return mypageService.getMyPosts(userId);
    }
}