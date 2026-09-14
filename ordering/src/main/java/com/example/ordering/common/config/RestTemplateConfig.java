package com.example.ordering.common.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// 동기요청을 위해 restTemplate를 사용, OrderingService에서 RestTemplate을 주입 받는다.

// LoadBalanced(부하 분산) : 서버에 가해지는 트래픽이나 작업 요청을 여러 대의 서버로 나누어 처리하는 기술
// 로드 밸런서(Load Balancer) : 클라이언트와 서버 그룹 사이의 요청을 여러 서버로 분해하는 장치나 소프트웨어
// @LoadBalanced => eureka에 등록되어 있는 서비스명을 사용해서 내부 서비스 호출(내부 통신)

// eureka에 등록되어 있는 서비스명 => "http://product-service/" => application.yaml에 등록
@Configuration
public class RestTemplateConfig {

    // Bean 객체를 만들면 싱글톤으로 객체가 만들어진다.
    // OrderingService에서 주입받는다.
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {

        return new RestTemplate();
    }
}
