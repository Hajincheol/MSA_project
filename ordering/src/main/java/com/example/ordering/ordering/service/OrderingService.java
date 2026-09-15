package com.example.ordering.ordering.service;

import com.example.ordering.ordering.domain.OrderStatus;
import com.example.ordering.ordering.domain.Ordering;
import com.example.ordering.ordering.dto.OrderCreateDTO;
import com.example.ordering.ordering.dto.ProductDTO;
import com.example.ordering.ordering.dto.ProductUpdateStockDTO;
import com.example.ordering.ordering.repository.OrderingRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
//import org.springframework.web.client.RestTemplate;

@Service
@Transactional
public class OrderingService {

    private final OrderingRepository orderingRepository;
    //private final RestTemplate restTemplate;

    // 동기 방식 => ProductFeign(추천) 훨씬 간결
    // private final RestTemplate restTemplate;
    private final ProductFeign productFeign;

    // 비동기 방식
    private final KafkaTemplate<String, Object> kafkaTemplate;


    public OrderingService(OrderingRepository orderingRepository,
                           //RestTemplate restTemplate,
                           ProductFeign productFeign,
                           KafkaTemplate<String, Object> kafkaTemplate) {

        this.orderingRepository = orderingRepository;
        //this.restTemplate = restTemplate;
        this.productFeign = productFeign;
        this.kafkaTemplate = kafkaTemplate;
    }


    /* 주문
    // 방식1. restTemplate
    // 매개변수에 String userId 추가
    public Ordering orderRestCreate(OrderCreateDTO orderCreateDTO, String userid) {
        System.out.println("<<< 서비스 - orderCreate >>>");

        // 1) 삭제
        // Product product = productRepository.findById(orderDTO.getProductId())
        //         .orElseThrow(() -> new EntityNotFoundException("product is not found"));

        // 2) product get 요청 - url 패턴으로 요청
        String productGetUrl = "http://product-service/product/" + orderCreateDTO.getProductId();


        // "X-User-Id"가 커스텀 헤더이므로 api-gateway에서 넘겨서 order까지만 유지됨, 따라서 order에서 아래처럼 다시 세팅해야함.
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("X-User-Id", userid);
        HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);

        // Get 방식으로 productGetUrl를 엔드 포인트로 주고 headers 정보를 들고 ProductDTO타입 객체로 돌려받는다.
        ResponseEntity<ProductDTO> response
                = restTemplate.exchange(productGetUrl, HttpMethod.GET, httpEntity, ProductDTO.class);

        // product에서 넘어온 결과
        ProductDTO productDTO = response.getBody();

        // 주문 수량
        int quantity = orderCreateDTO.getProductCount();

        // 재고수량 < 주문수량
        if(productDTO.getStockQuantity() < quantity) {

            throw new IllegalArgumentException("재고부족");
        } else {

            // 3) product put(제품 재고 업데이트) 요청 - url 패턴으로 요청
            String productPutUrl = "http://product-service/product/updatestock";
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            // 삭제
            // product.updateStockQuantity(orderDTO.getProductCount());

            HttpEntity<ProductUpdateStockDTO> updateEntity = new HttpEntity<>(

                    // body
                    ProductUpdateStockDTO
                            .builder()
                            .productId(orderCreateDTO.getProductId())
                            .productQuantity(orderCreateDTO.getProductCount())
                            .build()

                    // header
                    , httpHeaders
            );

            // productPutUrl로 put 요청시 updateEntity가 body에 세팅되어 나감
            restTemplate.exchange(productPutUrl, HttpMethod.PUT, updateEntity , Void.class);
        }


        // 주문 완성
        // .memberId(매개변수 userId)
        // .product(매개변수 orderDTO.getProduct())
        Ordering ordering = Ordering.builder()
                .memberId(Long.parseLong(userid))   // 주문자
                .productId(orderCreateDTO.getProductId())
                .quantity(orderCreateDTO.getProductCount())
                .build();

        orderingRepository.save(ordering);
        return ordering;
    }
    */


    // 방식2. OpenFeign 동기 처리
    // 방식3. Kafka 비동기 처리
    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackProductService")
    public Ordering openFeignKafkaOrderCreate(OrderCreateDTO dto, String memberId) {
        System.out.println("<<< OrderingService - openFeignKafkaOrderCreate >>>");

        // 제품 조회
        ProductDTO product = productFeign.selectByProductId(dto.getProductId());

        // 주문 수량 > 재고 수량
        if(dto.getProductCount() > product.getStockQuantity()) {
            throw new IllegalArgumentException("재고 부족");

        } else {

            ProductUpdateStockDTO productUpdateStockDTO =
                    ProductUpdateStockDTO
                            .builder()
                            .productId(dto.getProductId())
                            .productQuantity(dto.getProductCount())
                            .build();

            // openfeign 동기화 방식
            // productFeign.updateStock(productUpdateStockDTO);

            // 비동기 방식
            kafkaTemplate.send("update-stock-topic", productUpdateStockDTO);
        }


        // 주문 완성
        // .memberId(매개변수 userId)
        // .product(매개변수 orderDTO.getProduct())
        Ordering ordering = Ordering.builder()
                .memberId(Long.parseLong(memberId))   // 주문자
                .productId(dto.getProductId())
                .quantity(dto.getProductCount())
                .build();

        orderingRepository.save(ordering);
        return ordering;
    }

    public Ordering fallbackProductService(OrderCreateDTO dto, String memberId, Throwable throwable) {
        System.out.println("<<< OrderingService - fallbackProductService >>>");

        throw new RuntimeException("상품 서비스가 응답이 없어, 에러가 발생했습니다. 나중에 다시 해주세요.");
    }

    public void fallbackCancelOrderProductService(String oId, Throwable throwable) {
        System.out.println("<<< OrderingService - fallbackCancelOrderProductService >>>");

        throw new RuntimeException("상품 서비스가 응답이 없어, 에러가 발생했습니다. 나중에 다시 해주세요.");
    }

    // 특정 유저의 id로 검색
    public List<Ordering> selectByMemberId(String mId) {
        System.out.println("<<< OrderingService - selectByMemberId >>>");

        return orderingRepository.findByMemberId(Long.parseLong(mId));
    }

    // 주문 취소
    @CircuitBreaker(name = "orderProductService", fallbackMethod = "fallbackCancelOrderProductService")
    public void orderCancel(String oId) {
        System.out.println("<<< OrderingService - orderCancel >>>");

        Optional<Ordering> optionalOrdering = orderingRepository.findById(Long.parseLong(oId));

        if(optionalOrdering.isEmpty()) {
            throw new EntityNotFoundException("해당 주문이 없습니다.");

        } else {
            Ordering ordering = optionalOrdering.get();
            ProductDTO product = productFeign.selectByProductId(ordering.getProductId());

            // product 수량 원복
            product.setStockQuantity(product.getStockQuantity() + ordering.getQuantity());
            kafkaTemplate.send("canceled-ordering-topic", product);

            // ordering 상태 canceled로 전환
            orderingRepository.save(
                    Ordering.builder()
                            .id(ordering.getId())
                            .memberId(ordering.getMemberId())
                            .productId(ordering.getProductId())
                            .quantity(ordering.getQuantity())
                            .orderStatus(OrderStatus.CANCELED)
                            .build()
            );
        }
    }

    // 비동기 통신 kafka product id 관련 모든 주문 취소
    // 제품이 삭제될 예정이므로 모든 주문 취소 상태로 전환
    @KafkaListener(topics = "canceled-orders-topic", containerFactory = "kafkaListener")
    public void orderCancelByProductId(String message) {
        System.out.println("<<< OrderingService - orderCancelByProductId >>>");

        ObjectMapper objectMapper = new ObjectMapper();
        List<Long> productIdList = null;

        try {
            // List<Long>.class가 불가능하기에 new TypeReference<List<Long>>() {} 사용
            // 주는 쪽에서 forEach문으로 여러번 넣어서도 가능
            productIdList = objectMapper.readValue(message, new TypeReference<List<Long>>() {});

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


        List<Ordering> orderingList = orderingRepository.findByProductIdIn(productIdList);

        // 내부 반복으로 주문 상태 CANCELED로 변경
        orderingRepository.saveAll(

                orderingList.stream()
                        .map(o ->
                                Ordering.builder()
                                        .id(o.getId())
                                        .memberId(o.getMemberId())
                                        .productId(o.getProductId())
                                        .quantity(o.getQuantity())
                                        .orderStatus(OrderStatus.CANCELED)
                                        .build()
                        )
                        .toList()
        );
    }

    public Boolean existsByMemberId(Long mId) {
        System.out.println("<<< OrderingService - existsByMemberId >>>");

        return orderingRepository.existsByMemberIdAndOrderStatusEquals(mId, OrderStatus.ORDERED);
    }

    @KafkaListener(topics = "black-canceled-orders-topic", containerFactory = "kafkaListener")
    public void blackOrderCancelByMemberId(String message) {
        System.out.println("<<< OrderingService - blackOrderCancelByMemberId >>>");

        ObjectMapper objectMapper = new ObjectMapper();
        Long mId = null;

        try {
            mId = objectMapper.readValue(message, Long.class);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        List<Ordering> orderingList = orderingRepository.findByMemberId(mId);

        // 내부 반복으로 주문 상태 CANCELED로 변경

        kafkaTemplate.send("revert-stock-topic",

                // List<Ordering> -> List<ProductUpdateStockDTO>
                orderingList.stream()
                        .collect(
                                Collectors.groupingBy(
                                        Ordering::getProductId,
                                        Collectors.summingInt(Ordering::getQuantity)
                                )
                        )
                        .entrySet()
                        .stream()
                        .map(entry ->
                                ProductUpdateStockDTO.builder()
                                        .productId(entry.getKey())
                                        .productQuantity(entry.getValue())
                                        .build()
                        )
                        .toList()
        );

        // orderingList 수정
        orderingRepository.saveAll(
                orderingList.stream()
                        .map(o ->
                                Ordering.builder()
                                        .id(o.getId())
                                        .memberId(o.getMemberId())
                                        .productId(o.getProductId())
                                        .quantity(o.getQuantity())
                                        .orderStatus(OrderStatus.CANCELED)
                                        .build()
                        )
                        .toList()
        );
    }
}
