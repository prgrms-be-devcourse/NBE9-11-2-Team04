package com.back.devc.domain.member.member.repository;

import com.back.devc.domain.member.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    Optional<Member> findByEmail(String email);

    // 닉네임 검색 (부분 일치)
    Page<Member> findByNicknameContainingIgnoreCase(String nickname, Pageable pageable);

    // 이메일 검색 (부분 일치)
    Page<Member> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    // 닉네임 또는 이메일 통합 검색
    @Query("SELECT m FROM Member m WHERE " +
            "LOWER(m.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Member> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

}