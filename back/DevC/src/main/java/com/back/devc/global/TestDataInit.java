package com.back.devc.global;

import com.back.devc.domain.member.member.entity.Member;
import com.back.devc.domain.member.member.repository.MemberRepository;
import com.back.devc.domain.post.post.entity.Post;
import com.back.devc.domain.post.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestDataInit implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    @Override
    public void run(String... args) {

        Member member = new Member(1, "테스트유저");
        memberRepository.save(member);

        Post post = new Post(1, "테스트 게시글", member);
        postRepository.save(post);
    }
}