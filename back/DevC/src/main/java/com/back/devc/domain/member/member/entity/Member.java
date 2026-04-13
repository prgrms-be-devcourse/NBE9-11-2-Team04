package com.back.devc.domain.member.member.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Member {

    @Id
    private Integer userId;

    private String nickname;

    protected Member() {}

    public Member(Integer userId, String nickname) {
        this.userId = userId;
        this.nickname = nickname;
    }
}