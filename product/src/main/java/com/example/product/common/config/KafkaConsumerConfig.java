package com.example.product.common.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.kafka-server}")
    private String kafkaServer;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String offset;

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> config = new HashMap<>();

        // spring bean에 kafka 정보 설정
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);

        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offset);

        // message value를 dto -> json형태로 변환지정 cf)스프링부트에서 객체를 사용자에게 리턴,
        // 데이터 반환시  ObjectMapper.JsonSerializer가 dto -> json으로 자동변환
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(config);
    }

    // 위 consumerFactory() 메서드는 kafkaTemplate()의 요소로 들어감
    // ProductService => 생성된 ConcurrentKafkaListenerContainerFactory<Topic명, message>이 메시지를 수신
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListener() {
        ConcurrentKafkaListenerContainerFactory<String, String> listener
                = new ConcurrentKafkaListenerContainerFactory<>();

        listener.setConsumerFactory(consumerFactory());
        return listener;
    }
}
