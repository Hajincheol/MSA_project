package com.example.product.common.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.kafka-server}")
    private String kafkaServer;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> producerConfig = new HashMap<>();

        // spring bean에 kafka 정보 설정
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);

        // message key를 String 형태로 지정
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // message value를 dto -> json형태로 변환지정 cf)스프링부트에서 객체를 사용자에게 리턴,
        // 데이터 반환시  ObjectMapper.JsonSerializer가 dto -> json으로 자동변환
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(producerConfig);
    }

    // 위 producerFactory() 메서드는 kafkaTemplate()의 요소로 들어감
    // OrderingService => 생성된 KafkaTemplate<Topic명, message>이 send()로 메시지 발행
    // Bean객체인 KafkaTemplate.send(토픽명, 메시지);  => 토픽명:  StringSerializer, 메시지 : JsonSerializer
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
