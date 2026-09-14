package com.example.member.member.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

// @Component => 해당 클래스를 Spring Bean으로 등록
// 즉, spring Bean 해당 클래스의 객체를 생성하고 관리함

// @RefreshScope => @Value로 지정한 값은 spring cloud bus를 통해서 실시간으로 변경 가능한 값들이 된다.
@Component
@RefreshScope
public class JwtTokenProvider {

    // @Value => 설정값을 가져와서 변수에 주입해주는 것
    // application.yaml에 있는 jwt: expiration: 의 값을 가져옴(의존성 주입)
    @Value("${jwt.expiration}")
    private int expiration;

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.expirationRt}")
    private int expirationRt;

    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

    private Key ENCRYPT_AT_SECRET_KEY;
    private Key ENCRYPT_RT_SECRET_KEY;

    // 생성자가 호출되고, 스프링Bean이 만들어진 직후에 아래 메서드를 바로 실행하는 어노테이션
    @PostConstruct
    public void init() {

        System.out.println("<<< JwtTokenProvider - init() >>>");

        // SecretKeySpec(비밀키 데이터,
        // decoder => 원래 형태로 해석/복원
        // SignatureAlgorithm => JWT의 서명 방식을 선택하는 것
        // HS512 : JWT에서 사용할 HS512 서명 알고리즘
        // getJcaName() : 해당 알고리즘을 JAVA...(JCA)에서 사용하는 이름(String)을 가져옴
        ENCRYPT_AT_SECRET_KEY = new SecretKeySpec(Base64.getDecoder().decode(secretKey),
                                            SignatureAlgorithm.HS512.getJcaName());

        ENCRYPT_RT_SECRET_KEY = new SecretKeySpec(Base64.getDecoder().decode(secretKeyRt),
                                            SignatureAlgorithm.HS512.getJcaName());
    }

    // Access Token
    public String createAccessToken(String id, String role) {
        System.out.println("<<< JwtTokenProvider - createAccessToken >>>");

        // Claims => JWT Payload에 들어가는 데이터를 표현하는 객체
        // Jwts => JWT를 만들거나 파싱(읽고 구조를 해석)하는 기능을 제공하는 클래스 / 즉, JWT를 만들고 읽고 검증하는 도구

        // claims는 사용자 정보(페이로드 정보)
        // Claims를 만들고 해당 데이터에 sub라는 이름으로 id를 넣어라 / role이라는 이름으로 role을 넣어라
        Claims claims = Jwts.claims().setSubject(id);
        claims.put("role", role);

        Date now = new Date();
        String accessToken = Jwts.builder()
                .setClaims(claims)      // payload에 claims 넣기
                .setIssuedAt(now)       // 토큰이 발급된 시간
                .setExpiration(new Date(now.getTime() + expiration*60*1000L))   // 토큰의 만료 시간
                .signWith(ENCRYPT_AT_SECRET_KEY)   // 비밀키를 이용해서 JWT에 서명(Signature)
                .compact(); // 지금까지 설정한 내용을 실제 JWT 문자열 형태로 완성


        return accessToken;
    }


    // Refresh Token
    public String createRefreshToken(String email, String role) {
        System.out.println("<<< JwtTokenProvider - createRefreshToken >>>");

        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);

        Date now = new Date();
        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationRt*60*1000L))
                .signWith(ENCRYPT_RT_SECRET_KEY)
                .compact();


        return refreshToken;
    }

}
