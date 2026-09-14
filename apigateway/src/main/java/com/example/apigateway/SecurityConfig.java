package com.example.apigateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {


    // CorsConfigurationSource : CORS 설정을 제공하는 역할을 정의한 인터페이스
    private CorsConfigurationSource corsConfiguration() {
        System.out.println("<<< SecurityConfig - corsConfiguration >>>");

        // CorsConfiguration : CORS 규칙을 담는 객체
        // 어떤 Origin, HTTP Method, Header 등을 허용할 것인지 설정하는 객체
        CorsConfiguration configuration = new CorsConfiguration();

        // port - 3000 프론트엔드(Origin) 요청만 허용
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));

        // 모든 HTTP(get, post 등) 메서드 허용
        configuration.setAllowedMethods(Arrays.asList("*"));

        // 모든 헤더 허용
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 자격 증명 허용
        // 다른 Origin에서 오는 요청이 인증 정보(Cookie 등)를 포함하는 것을 허용(true)
        configuration.setAllowCredentials(true);

        // UrlBasedCorsConfigurationSource : 모든 url 패턴에 대해 어떤 cors 설정 적용 관리 클래스
        // registerCorsConfiguration : 적용할 URL 패턴을 등록하는 메서드
        // "/**" 하위에 있는 모든 것
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity httpSecurity) throws Exception {


        return httpSecurity
                .cors(c -> c.configurationSource(corsConfiguration()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }
}
