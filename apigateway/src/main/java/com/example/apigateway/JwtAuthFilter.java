package com.example.apigateway;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.List;

// apigate에서는 neti 기반의 비동기 프로젝트라서 일반적으로 그것에 최적화되어 있는 GlobalFilter를 사용한다.
// cf) 보통 스프링부트에서 로그인 개발을 하거나 검증을 할때 스프링시큐리티, 톰캣기반의 동기프로젝트를 사용한다.
@Component
public class JwtAuthFilter implements GlobalFilter {   // neti 기반의 비동기

    @Value("${jwt.secretKey}")
    private String secretKey;     // application.yml 마지막에 설정한 값을 가져옴, 토큰을 만든 시점의 secretKey와 동일해야 함

    // 어떤 url path를 허용할지에 대한 내용임, 검증대상이 아니고
    private static final List<String> ALLOWED_PATHS = List.of(
            "/member/signup",
            "/member/login",
            "/member/refresh-token",   // 토큰갱신
            "/product/list",    // 상품목록은 회원이 아니어도 조회가능하도록
            "/member/logout"
    );

    // 1) cors(CorsWebFilter) : application.yml    → 2) token검증(GlobalFilter)     →  3) 라우팅 처리(GatewayFilter)
    // 1) SecurityFilterCharin  2) implements GlobalFilter   3) SecurityFilterChain의  return chain.filter => 라우팅 처리
    // 인증요청에서 제외
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // token 검증
        System.out.println("token 검증 시작");
        String bearerToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String path = exchange.getRequest().getURI().getRawPath();  // localhost:8080/member-service/member/doLogin 에서 뒤부분 member/doLogin
        System.out.println(path);
        // 인증이 필요 없는 경로는 필터를 통과
        if (ALLOWED_PATHS.contains(path)) {
            return chain.filter(exchange); // => 세번째 라우팅처리로 간다
        }

        // 인증이 필요한 모든 코드는 아래 token filter로 넘어감, 토큰이 없는데 토큰을 검증할 경우 에러발생
        try {
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                throw new IllegalArgumentException("token 관련 예외 발생");
            }
            String token = bearerToken.substring(7);  // 토큰에서 Bearer를 제외한 인덱스 7번째 이상부터 가져온다.

            // token 검증 및 claims 추출
            // Jwts는 build.gradle에 jwt토큰 관련 라이브러리로 추가되어 있어야 import 가능함 => import io.jsonwebtoken.*; 가능

            // 1. 토큰안 payload에 사용자정보를 세팅해서 넣어뒀는데 모노리식에서는 member, product, ordering에서 쉽게 꺼내볼수 있었으나
            // msa에서는 꺼내볼수 없게 설계함. 토큰 검증은 apigateway에서 하도록 설계했으므로
            // 2. 하지만 각 msa에서 사용자ID, role이 필요하므로 header에 추가함
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // 모노리식과 mas가 다른 부분
            // 2-1. 토큰에서 사용자 ID, role 추출
            String userId = claims.getSubject();
            String role = claims.get("role", String.class);



            // 2-2. 헤더에 X-User-Id변수로 id값 추가 및 ROLE 추가
            // X를 붙이는 것은 custom header라는 것을 의미하는 널리 쓰이는 관례
            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(builder -> builder
                            .header("X-User-Id", userId)
                            .header("X-User-Role", "ROLE_" + role) // 역할 추가
                    )
                    .build();

            // Spring Cloud Gateway는 여러 필터를 GatewayFilterChain이라는 구조로 관리
            // 2-3. modifiedExchange를 담아서 다시 filter chain으로 되돌아 가는 로직.
            // 2-4. member-service로 전달됨(application-yml의 routes: uri: lb://member-service), 같은 방식으로 나머지 서비스로 전달
            return chain.filter(modifiedExchange);
        } catch (IllegalArgumentException | MalformedJwtException | ExpiredJwtException | SignatureException |
                 UnsupportedJwtException e) {
            e.printStackTrace();
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
}

