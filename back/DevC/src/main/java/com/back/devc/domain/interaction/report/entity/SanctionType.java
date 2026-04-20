package com.back.devc.domain.interaction.report.entity;

public enum SanctionType {
    ACTIVE,
    WARNED, // 경고
    SUSPENDED, //기간 정지
    BLACKLISTED, // 차단
    WITHDRAWN
}