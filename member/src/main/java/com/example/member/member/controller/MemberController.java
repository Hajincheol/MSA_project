package com.example.member.member.controller;

import com.example.member.member.domain.Member;
import com.example.member.member.domain.Role;
import com.example.member.member.dto.LoginDTO;
import com.example.member.member.dto.MemberRefreshDTO;
import com.example.member.member.dto.MemberSaveRequestDTO;
import com.example.member.member.service.JwtTokenProvider;
import com.example.member.member.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/member")
public class MemberController {

    // Redis(Remote Dictionary Server)는 데이터를 디스크가 아닌
    // 컴퓨터 메모리(RAM)에 저장하여 매우 빠르게 읽고 쓸 수 있는 오픈 소스 인메모리
    // 키-값(Key-Value) 데이터 구조 스토어이자 NoSQL 데이터베이스입니다.

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    // Bean : Spring이 생성하고 관리하는 객체
    // RedisTemplate Bean : Spring이 관리하는 RedisTemplate 객체
    // @Qualifier("rtdb") => 여러 RedisTemplate Bean 중 이름이 rtdb인 것을 선택
    // NoSQL => 관계형 데이터베이스가 아닌 다른 방식으로 데이터를 저장하는 DB
    // Redis => Key-Value 구조를 중심으로 사용하는 NoSQL 데이터 저장소
    // RedisTemplate => Redis와 데이터를 주고받는 객체 => 해당 객체로 Redis랑 통신함
    @Qualifier("rtdb")
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;


    // @RequiredArgsConstructor로 대체 가능
    public MemberController(MemberService memberService,
                            JwtTokenProvider jwtTokenProvider,
                            @Qualifier("rtdb") RedisTemplate<String, Object> redisTemplate) {
        System.out.println("<<< MemberController - 생성자 >>>");

        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
    }


    // 회원가입
    // ResponseEntity<> : 클라이언트(React)에게 어떤 응답을 보낼지 포장하는 객체
    // => 상태 코드(Http Status), 헤더(Header), 본문(Body)을 다룸
    // Body => 실제 전달 데이터, Header => Body의 데이터 형식 or 통신 정보
    @PostMapping("/signup")
    public ResponseEntity<?> memberCreate(@RequestBody MemberSaveRequestDTO dto) {
        System.out.println("<<< MemberController - memberCreate >>>");

        Long id = memberService.save(dto);
        return new ResponseEntity<>(id, HttpStatus.CREATED);
    }


    // 로그인, 로그아웃--------------------------------------------------------------
    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO dto) {
        System.out.println("<<< MemberController - login >>>");

        Member member = memberService.login(dto);

        // 회원이 ban되었을 경우 403 접근 권한 없음 return
        if(member.getRole().equals(Role.BLACK)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getId().toString(), member.getRole().toString());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail(), member.getRole().toString());

        redisTemplate.opsForValue().set(member.getEmail(), refreshToken, 200, TimeUnit.DAYS);

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", member.getId());
        loginInfo.put("accessToken", accessToken);
        loginInfo.put("refreshToken", refreshToken);

        return new ResponseEntity<>(loginInfo, HttpStatus.OK);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody MemberRefreshDTO dto) {
        System.out.println("<<< MemberController - logout >>>");

        redisTemplate.delete(dto.getRefreshToken());

        return new ResponseEntity<>(HttpStatus.OK);
    }


    // 마이페이지 ----------------------------------------------------------
    // 내 정보
    @PostMapping("/mypage/myinfo")
    public ResponseEntity<?> myPageInfo(@RequestHeader("X-User-Id") String id) {
        System.out.println("<<< MemberController - myPageInfo >>>");

        Member member = memberService.myPageInfo(id);

        Map<String, Object> user = new HashMap<>();
        user.put("name", member.getName());
        user.put("email", member.getEmail());

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    // 이름 변경
    @PostMapping("/mypage/nameChange/{name}")
    public ResponseEntity<?> nameChange(@PathVariable String name, @RequestHeader("X-User-Id") String id) {
        System.out.println("<<< MemberController - nameChange >>>");

        Member member = memberService.nameChange(id, name);

        String accessToken = jwtTokenProvider.createAccessToken(member.getId().toString(), member.getRole().toString());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail(), member.getRole().toString());

        // accessToken 재발급을 위해 => value 값인 refreshToken 변경
        redisTemplate.opsForValue().set(member.getEmail(), refreshToken, 200, TimeUnit.DAYS);

        Map<String, Object> newLoginInfo = new HashMap<>();
        newLoginInfo.put("accessToken", accessToken);
        newLoginInfo.put("refreshToken", refreshToken);

        return new ResponseEntity<>(newLoginInfo, HttpStatus.OK);
    }

    // 사용자 상태 확인
    @PostMapping("/memberStatus")
    public ResponseEntity<?> memberStatus(@RequestHeader("X-User-Id") String id) {
        System.out.println("<<< MemberController - memberStatus >>>");

        return new ResponseEntity<>(memberService.memberStatus(id));
    }

    // --------------------------------------------------------------------
    // 모든 페이지에서 사용 - accessToken 만료시 refreshToken으로 새로운 accessToken 생성
    // accessToken 재발급
    @PostMapping("/refresh-token")
    public ResponseEntity<?> generateNewAt(@RequestBody MemberRefreshDTO dto) {
        System.out.println("<<< MemberController - generateNewAt >>>");

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKeyRt)
                .build()
                .parseClaimsJws(dto.getRefreshToken())
                .getBody();


        Object rt = redisTemplate.opsForValue().get(claims.getSubject());

        if(rt == null || !rt.toString().equals(dto.getRefreshToken())) {
            return new ResponseEntity<>((Object) null, HttpStatus.BAD_REQUEST);
        }


        String accessToken = jwtTokenProvider.createAccessToken(claims.getSubject(), claims.get("role").toString());
        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("accessToken", accessToken);


        return new ResponseEntity<>(loginInfo, HttpStatus.OK);
    }


    // 관리자용------------------------------------------
    // 관리자 admin인지 확인
    @PostMapping("/admin")
    public ResponseEntity<?> checkAdmin(@RequestHeader("X-User-Role") String role) {
        System.out.println("<<< MemberController - checkAdmin >>>");

        if(!role.equals("ROLE_ADMIN")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);

        } else {
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }


    // admin만 가능 => 모든 member list 확인
    @PostMapping("/list")
    public ResponseEntity<?> memberList(@RequestHeader("X-User-Role") String role) {
        System.out.println("<<< MemberController - memberList >>>");

        if(!role.equals("ROLE_ADMIN")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);

        } else {
            return new ResponseEntity<>(memberService.memberList(role), HttpStatus.OK);
        }
    }


    // admin만 가능 => BLACK or USER 상태 변경
    @PostMapping("/changeStatus/{id}")
    public ResponseEntity<?> memberChangeStatus(@RequestHeader("X-User-Role") String role, @PathVariable String id) {
        System.out.println("<<< MemberController - memberChangeStatus >>>");

        if(!role.equals("ROLE_ADMIN")) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        } else {
            memberService.memberChangeStatus(Long.parseLong(id));
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }
}
