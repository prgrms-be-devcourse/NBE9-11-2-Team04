package com.back.devc.domain.member.member.controller;

import com.back.devc.domain.member.member.dto.AdmMemberDetailResponse;
import com.back.devc.domain.member.member.dto.AdmMemberListResponse;
import com.back.devc.domain.member.member.dto.AdmMemberStatusUpdateRequest;
import com.back.devc.domain.member.member.service.AdmMemberService;
import com.back.devc.global.response.SuccessCode;
import com.back.devc.global.response.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
public class AdmMemberController {

    private final AdmMemberService adminMemberService;

    // 1. 전체 회원 목록 조회
    @GetMapping
    public ResponseEntity<SuccessResponse<Page<AdmMemberListResponse>>> getMembers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<AdmMemberListResponse> response = adminMemberService.getMembers(page, size);
        SuccessCode successCode = SuccessCode.MEMBER_LIST_SUCCESS;
        return ResponseEntity
                .status(successCode.getStatus())
                .body(SuccessResponse.of(successCode, response));
    }

    // 2. 회원 상세 조회
    @GetMapping("/{userId}")
    public ResponseEntity<SuccessResponse<AdmMemberDetailResponse>> getMemberDetail(
            @PathVariable Long userId
    ) {
        AdmMemberDetailResponse response = adminMemberService.getMemberDetail(userId);
        SuccessCode successCode = SuccessCode.MEMBER_DETAIL_SUCCESS;
        return ResponseEntity
                .status(successCode.getStatus())
                .body(SuccessResponse.of(successCode, response));
    }

    // 3. 회원 상태 변경
    @PatchMapping("/{userId}/status")
    public ResponseEntity<SuccessResponse<AdmMemberDetailResponse>> updateMemberStatus(
            @PathVariable Long userId,
            @RequestBody @Valid AdmMemberStatusUpdateRequest request
    ) {
        AdmMemberDetailResponse response = adminMemberService.updateMemberStatus(userId, request);
        SuccessCode successCode = SuccessCode.MEMBER_STATUS_UPDATE_SUCCESS;
        return ResponseEntity
                .status(successCode.getStatus())
                .body(SuccessResponse.of(successCode, response));
    }

    // 4. 회원 검색
    @GetMapping("/search")
    public ResponseEntity<SuccessResponse<Page<AdmMemberListResponse>>> searchMembers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<AdmMemberListResponse> response = adminMemberService.searchMembers(keyword, page, size);
        SuccessCode successCode = SuccessCode.MEMBER_SEARCH_SUCCESS;
        return ResponseEntity
                .status(successCode.getStatus())
                .body(SuccessResponse.of(successCode, response));
    }
}