package com.example.member.member.service;

import com.example.member.member.domain.Member;
import com.example.member.member.domain.Role;
import com.example.member.member.repository.MemberRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// CommandLineRunner를 구현함으로써 해당 컴포넌트가 스프링빈으로 등록되는 시점에서 run 메서드 자동 실행
// @Component => 해당 클래스를 Spring Bean으로 등록
// 즉, spring Bean 해당 클래스의 객체를 생성하고 관리함
@Component
public class InitialDataLoader implements CommandLineRunner {

    // PasswordEncoder : 비밀번호를 해시하기 위한 인터페이스(암호화한다고 이해해도 됨)
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public InitialDataLoader(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        System.out.println("<<< InitialDataLoader - 생성자 >>>");

        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }


    // CommandLineRunner이 구현된 InitialDataLoader가 Bean으로 등록되는 시점에 run이 자동 실행된다.
    @Override
    public void run(String... args) throws Exception {
        System.out.println("<<< InitialDataLoader - run >>>");

        if(memberRepository.findByEmail("admin@naver.com").isPresent()) {
            return;
        }

        Member member = Member.builder()
                .name("admin")
                .email("admin@naver.com")
                .password(passwordEncoder.encode("admin147*369"))
                .role(Role.ADMIN)
                .build();


        memberRepository.save(member);
    }
}
