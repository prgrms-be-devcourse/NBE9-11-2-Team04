package com.back.devc.domain.member.searchLog.controller;

import com.back.devc.domain.member.searchLog.dto.CreateSearchLogRequest;
import com.back.devc.domain.member.searchLog.dto.PopularKeywordResponse;
import com.back.devc.domain.member.searchLog.dto.SearchLogResponse;
import com.back.devc.domain.member.searchLog.service.SearchLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SearchLogController {

    private final SearchLogService searchLogService;

    @PostMapping("/search-logs")
    public SearchLogResponse createSearchLog(
            @RequestParam Long userId,
            @RequestBody CreateSearchLogRequest request
    ) {
        return searchLogService.createSearchLog(userId, request);
    }

    @GetMapping("/users/me/search-logs")
    public List<SearchLogResponse> getMySearchLogs(@RequestParam Long userId) {
        return searchLogService.getMySearchLogs(userId);
    }

    @DeleteMapping("/users/me/search-logs/{searchLogId}")
    public void deleteSearchLog(
            @RequestParam Long userId,
            @PathVariable Long searchLogId
    ) {
        searchLogService.deleteSearchLog(userId, searchLogId);
    }

    @DeleteMapping("/users/me/search-logs")
    public void deleteAllSearchLogs(@RequestParam Long userId) {
        searchLogService.deleteAllSearchLogs(userId);
    }

    @GetMapping("/search-logs/popular")
    public List<PopularKeywordResponse> getPopularKeywords() {
        return searchLogService.getPopularKeywords();
    }
}
