package com.back.devc.domain.member.member.repository;

import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.entity.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    Optional<Member> findByEmail(String email);

    Page<Member> findByStatus(MemberStatus status, Pageable pageable);

    Page<Member> findByNicknameContainingOrEmailContaining(
            String nickname,
            String email,
            Pageable pageable
    );

    Page<Member> findByStatusAndNicknameContainingOrStatusAndEmailContaining(
            MemberStatus status1,
            String nickname,
            MemberStatus status2,
            String email,
            Pageable pageable
    );
}