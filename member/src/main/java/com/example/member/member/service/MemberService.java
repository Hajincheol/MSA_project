package com.example.member.member.service;

import com.example.member.member.domain.Member;
import com.example.member.member.domain.Role;
import com.example.member.member.dto.LoginDTO;
import com.example.member.member.dto.MemberSaveRequestDTO;
import com.example.member.member.repository.MemberRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MemberService {

    // PasswordEncoder : 비밀번호를 해시하기 위한 인터페이스(암호화한다고 이해해도 됨)
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProductFeign productFeign;
    private final OrderingFeign orderingFeign;
    private final KafkaTemplate<String, Object> kafkaTemplate;


    // @RequiredArgsConstructor로 대체 가능
    public MemberService(MemberRepository memberRepository,
                         PasswordEncoder passwordEncoder,
                         ProductFeign productFeign,
                         OrderingFeign orderingFeign,
                         KafkaTemplate<String, Object> kafkaTemplate) {

        System.out.println("<<< MemberService - 생성자 >>>");

        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.productFeign = productFeign;
        this.orderingFeign = orderingFeign;
        this.kafkaTemplate = kafkaTemplate;
    }


    // 회원가입, 로그인 --------------------------------------------------
    // 회원가입
    public Long save(MemberSaveRequestDTO memberSaveRequestDTO) {

        System.out.println("<<< MemberService - save >>>");

        // Optional => 값이 있을 수도 있고 없을 수도 있는 상황을 안전하게 처리하기 위한 클래스
        // 즉, 값이 존재할 수도, 존재하지 않을 수도 있음을 표현하는 객체

        // isEmpty() : 값 없는 여부
        // isPresent() : 값 존재 여부
        // orElse() : 값이 없으면 지정 기본값 반환
        // orElseGet() : 값이 없으면 실행할 함수 지정
        // orElseThrow() : 값이 없으면 예외 발생
        Optional<Member> optionalMember = memberRepository.findByEmail(memberSaveRequestDTO.getEmail());
        if(optionalMember.isPresent()) {
            throw new IllegalArgumentException("기존에 존재하는 회원입니다.");
        }

        String password = passwordEncoder.encode(memberSaveRequestDTO.getPassword());
        Member member = memberRepository.save(memberSaveRequestDTO.toEntity(password));

        return member.getId();
    }

    // 로그인
    public Member login(LoginDTO dto) {
        System.out.println("<<< MemberService - login >>>");

        // 스위칭 기법
        boolean check = true;

        // email 존재 여부
        // matches : 사용자 입력 pw와 db에 저장된 암호화된 password와 비교
        Optional<Member> optionalMember = memberRepository.findByEmail(dto.getEmail());
        if(optionalMember.isEmpty()) {
            check = false;

        } else if(!passwordEncoder.matches(dto.getPassword(), optionalMember.get().getPassword())) {
            check = false;
        }

        if(!check) {
            throw new IllegalArgumentException("email 또는 비밀번호가 일치하지 않습니다.");
        }

        return optionalMember.get();
    }

    // 마이페이지 -----------------------------------------------------
    // 내 정보
    public Member myPageInfo(String id) {
        System.out.println("<<< MemberService - myPageInfo >>>");

        Optional<Member> member = memberRepository.findById(Long.parseLong(id));

        if(member.isPresent()) {
            return member.get();
        }

        return null;
    }

    // 이름 변경
    public Member nameChange(String id, String name) {
        System.out.println("<<< MemberService - nameChange >>>");

        Optional<Member> optionalMember = memberRepository.findById(Long.parseLong(id));
        if(optionalMember.isPresent()) {
            Member member = optionalMember.get();

            return memberRepository.save(
                    Member.builder()
                            .id(member.getId())
                            .name(name)
                            .email(member.getEmail())
                            .password(member.getPassword())
                            .role(member.getRole())
                            .build()
            );
        }

        return null;
    }


    // admin만 가능 ----------------------------------------------------
    // kafka AND circuitBreaker
    // admin만 가능 => BLACK or USER 상태 변경
    @CircuitBreaker(name = "memberStatusService", fallbackMethod = "fallbackMemberService")
    public void memberChangeStatus(Long id) {
        System.out.println("<<< MemberService - memberChangeStatus >>>");

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(("해당 회원이 존재하지 않습니다.")));

        if(member.getRole().equals(Role.ADMIN)) {
            throw new IllegalArgumentException("관리자 상태를 변경할 수 없습니다");

        } else if(member.getRole().equals(Role.USER)) {

            // 해당 사용자를 BLACK으로 변경을 위해
            // 해당 user의 모든 등록 제품, 제품 주문의 존재를 확인
            if(orderingFeign.existsOrderingByMemberId(id)) {

                // 해당 user의 모든 등록 제품 삭제
                kafkaTemplate.send("black-canceled-orders-topic", id);
            }
            if(productFeign.existsProductByMemberId(id)) {

                // 해당 user의 모든 등록 제품 삭제
                kafkaTemplate.send("black-user-product-disabled", id);
            }

            memberRepository.save(
                    Member.builder()
                            .id(member.getId())
                            .name(member.getName())
                            .email(member.getEmail())
                            .password(member.getPassword())
                            .role(Role.BLACK)
                            .build()
            );

        } else if(member.getRole().equals(Role.BLACK)) {

            memberRepository.save(
                    Member.builder()
                            .id(member.getId())
                            .name(member.getName())
                            .email(member.getEmail())
                            .password(member.getPassword())
                            .role(Role.USER)
                            .build()
            );
        }
    }

    // memberChangeStatus의 circuitBreaker fallbackMethod
    public void fallbackMemberService(Long id, Throwable throwable) {
        System.out.println("<<< MemberService - fallbackMemberService >>>");

        throw new RuntimeException("제품 삭제 및 주문 취소에 실패했습니다. 나중에 다시 시도해 주세요.");
    }

    // 사용자 상태 확인
    public HttpStatus memberStatus(String id) {
        System.out.println("<<< MemberService - memberStatus >>>");

        Optional<Member> member = memberRepository.findById(Long.parseLong(id));
        if(member.isPresent()) {
            if(member.get().getRole().equals(Role.USER) || member.get().getRole().equals(Role.ADMIN)) {
                return HttpStatus.OK;
            } else {
                return HttpStatus.FORBIDDEN;
            }
        } else {
            return HttpStatus.NOT_FOUND;
        }
    }

    // admin만 가능, 회원 list
    public List<Member> memberList(String role) {
        System.out.println("<<< MemberService - memberList >>>");

        if(role.equals("ROLE_ADMIN")) {

            List<Member> memberList = memberRepository.findAll();

            return memberList;
        } else {
            return null;
        }
    }
}
