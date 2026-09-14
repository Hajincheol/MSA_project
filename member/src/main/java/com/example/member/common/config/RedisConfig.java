package com.example.member.common.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    // redis에 접근하기 윈한 접근(connection) 객체
    @Bean
    @Qualifier("rtdb")
    public RedisConnectionFactory redisConnectionFactory() {
        System.out.println("<<< RedisConfig - redisConnectionFactory >>>");

        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();

        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(0);

        return new LettuceConnectionFactory(configuration);
    }

    // redis에 저장할 key, value의 타입 지정한 template 객체 생성
    // redisTemplate이라는 메서드가 config 전체에 1개는 있어야 함.
    @Bean
    @Qualifier("rtdb")
    public RedisTemplate<String, Object> redisTemplate(@Qualifier("rtdb") RedisConnectionFactory redisConnectionFactory) {
        System.out.println("<<< RedisConfig - redisTemplate >>>");

        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        return redisTemplate;
    }
}
