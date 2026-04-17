package com.back.devc.domain.member.mypage.controller;

import com.back.devc.domain.interaction.bookmark.dto.BookmarkedPostResponse;
import com.back.devc.domain.interaction.postLike.dto.LikedPostResponse;
import com.back.devc.domain.member.mypage.dto.MyCommentResponse;
import com.back.devc.domain.member.mypage.dto.MyPostResponse;
import com.back.devc.domain.member.mypage.dto.MyProfileResponse;
import com.back.devc.domain.member.mypage.dto.UpdateMyProfileRequest;
import com.back.devc.domain.member.mypage.service.MypageService;
import com.back.devc.global.exception.ApiException;
import com.back.devc.global.exception.ErrorCode;
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

    @GetMapping
    public MyProfileResponse getMyProfile(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        validatePrincipal(principal);
        return mypageService.getMyProfile(principal.userId());
    }

    @GetMapping("/posts")
    public List<MyPostResponse> getMyPosts(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        validatePrincipal(principal);
        return mypageService.getMyPosts(principal.userId());
    }

    @GetMapping("/comments")
    public List<MyCommentResponse> getMyComments(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        validatePrincipal(principal);
        return mypageService.getMyComments(principal.userId());
    }

    @GetMapping("/likes")
    public List<LikedPostResponse> getMyLikedPosts(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        validatePrincipal(principal);
        return mypageService.getMyLikedPosts(principal.userId());
    }

    @GetMapping("/bookmarks")
    public List<BookmarkedPostResponse> getMyBookmarkedPosts(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        validatePrincipal(principal);
        return mypageService.getMyBookmarkedPosts(principal.userId());
    }

    @PatchMapping
    public MyProfileResponse updateMyProfile(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestBody @Valid UpdateMyProfileRequest request
    ) {
        validatePrincipal(principal);
        return mypageService.updateMyProfile(principal.userId(), request);
    }

    private void validatePrincipal(JwtPrincipal principal) {
        if (principal == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
    }
}