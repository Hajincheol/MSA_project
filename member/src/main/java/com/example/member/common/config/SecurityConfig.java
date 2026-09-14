package com.example.member.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder makePassword() {
        System.out.println("<<< SecurityConfig - makePassword >>>");

        // PasswordEncoderFactories : Spring Security에서 사용할 PasswordEncoder를 쉽게 만들어주는 클래스
        // DelegatingPasswordEncoder : 여러 PasswordEncoder와 관련된 클래스가 있고 해당 작업을 위임하는 클래스
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
