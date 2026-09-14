package com.example.product.product.service;

import com.example.product.product.domain.Product;
import com.example.product.product.dto.ProductRegisterDTO;
import com.example.product.product.dto.ProductResponseDTO;
import com.example.product.product.dto.ProductUpdateStockDTO;
import com.example.product.product.repository.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class ProductService {

    // 필드
    private final ProductRepository productRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // 매개변수 생성자
    // @RequiredArgsConstructor 대체 가능
    public ProductService(ProductRepository productRepository,
                          KafkaTemplate<String, Object> kafkaTemplate) {
        System.out.println("<<< ProductService - 생성자 >>>");

        this.kafkaTemplate = kafkaTemplate;
        this.productRepository = productRepository;
    }

    // 제품 등록 및 수정
    public void productSave(ProductRegisterDTO dto, String userId) {
        System.out.println("<<< ProductService - productSave >>>");

        Product product = dto.toEntity(Long.parseLong(userId));
        if(product.getId() != null) {

            // 제품 수정
            productRepository.save(product);

            List<Long> id = new ArrayList<>();
            id.add(product.getId());
            kafkaTemplate.send("canceled-orders-topic", id);

        } else {

            // 제품 생성
            productRepository.save(product);
        }
    }

    // restTemplate
    // 지정 제품 재고 상세 조회
    public ProductResponseDTO productSelectById(Long id) {
        System.out.println("<<< ProductService - productSelectById >>>");

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 제품입니다."));

        ProductResponseDTO productResponseDTO = ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .build();

        return productResponseDTO;
    }


    // 검색한 내용이 포함된 제품명을 조회
    public List<Product> productSelectByName(String pName) {
        System.out.println("<<< ProductService - productSelectByName >>>");

        Optional<List<Product>> optionalProducts = productRepository.findByNameContaining(pName);
        if(optionalProducts.isPresent()) {
            return optionalProducts.get();
        } else {
            return null;
        }
    }


    // 해당 제품으로 주문된 모든 주문 list 조회
    public List<Product> orderingProductList(List<Long> pId) {
        System.out.println("<<< ProductService - orderingProductList >>>");

        return productRepository.findAllByIdInOrderById(pId);
    }


    // Kafka
    // listener 객체가 실시간으로 update-stock-topic을 바라보고 있다가 메시지가 들어오면 매개변수에 값이 들어가고 메서드 실행
    @KafkaListener(topics = "update-stock-topic", containerFactory = "kafkaListener")
    public void updateStockConsumer(String message) {
        System.out.println("<<< 서비스 - updateStockConsumer() >>>");

        ObjectMapper objectMapper = new ObjectMapper();
        ProductUpdateStockDTO dto = null;

        try {
            // ObjectMapper라는 라이브러리를 통해서 message를 객체로 형변환
            dto = objectMapper.readValue(message, ProductUpdateStockDTO.class);

        } catch(JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // 상품 재고 업데이트 API 호출, 즉 order 호출할 때마다 product 재고가 1씩 감소
        this.updateStockQuantity(dto);
    }

    // restTemplate
    // 지정 제품 재고 수량 변경
    // 재고 감소
    public Product updateStockQuantity(ProductUpdateStockDTO productUpdateStockDTO) {
        System.out.println("<<< ProductService - updateStockQuantity >>>");

        Product product = productRepository.findById(productUpdateStockDTO.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 제품입니다."));

        product.updateStockQuantity(productUpdateStockDTO.getProductQuantity());

        return product;
    }

    // kafka
    @KafkaListener(topics = "canceled-ordering-topic", containerFactory = "kafkaListener")
    public void canceledOrdering(String message) {
        System.out.println("<<< 서비스 - canceledOrdering() >>>");

        ObjectMapper objectMapper = new ObjectMapper();
        ProductResponseDTO dto = null;

        try {
            dto = objectMapper.readValue(message, ProductResponseDTO.class);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Optional<Product> optionalProduct = productRepository.findById(dto.getId());

        if(optionalProduct.isPresent()) {
            Product product = optionalProduct.get();

            productRepository.save(
                    Product.builder()
                            .id(product.getId())
                            .category(product.getCategory())
                            .name(dto.getName())
                            .price(dto.getPrice())
                            .stockQuantity(dto.getStockQuantity())
                            .memberId(product.getMemberId())
                            .build()
            );
        }

    }

    // 특정 유저의 id로 검색
    // 마이페이지
    public List<Product> selectByMemberId(String mId) {
        System.out.println("<<< ProductService - selectByMemberId >>>");

        Optional<List<Product>> optionalProduct = productRepository.findByMemberId(Long.parseLong(mId));

        if(optionalProduct.isPresent()) {

            List<Product> productList = optionalProduct.get();
            List<Product> myProductList = new ArrayList<>();

            for(int i=0; i<productList.size(); i++) {

                if(productList.get(i).getStockQuantity() > 0) {
                    myProductList.add(productList.get(i));
                }
            }

            return myProductList;

        } else {
            return null;
        }
    }

    // 제품 목록 조회
    // 제품 목록
    public List<Product> productList() {
        System.out.println("<<< ProductService - productList >>>");

        List<Product> productList = productRepository.findAll();
        List<Product> productListValid = new ArrayList<>();

        for(Product product : productList) {
            if(product.getStockQuantity() > 0) {

                productListValid.add(product);
            }
        }

        return productListValid;
    }

    // 제품 삭제
    @CircuitBreaker(name="orderingService", fallbackMethod = "fallbackOrderingService")
    public void productDelete(String p_id) {
        System.out.println("<<< ProductService - productDelete >>>");

        // 제품이 사라졌으므로 모든 order는 CANCELED
        kafkaTemplate.send("canceled-orders-topic", p_id);
        productRepository.deleteById(Long.parseLong(p_id));
    }

    // circuitBreaker 발생시
    public Product fallbackOrderingService(String p_id, Throwable throwable) {
        System.out.println("<<< ProductService - fallbackOrderingService >>>");

        throw new RuntimeException("페이지에 응답이 없어, 에러가 발생했습니다. 나중에 다시 해주세요.");
    }


    public Boolean existsByMemberId(Long m_id) {
        System.out.println("<<< ProductService - existsByMemberId >>>");

        return productRepository.existsByMemberId(m_id);
    }


    @KafkaListener(topics = "black-user-product-delete", containerFactory = "kafkaListener")
    public void blackUserProductDelete(String message) {
        System.out.println("<<< ProductService - blackUserProductDelete >>>");

        ObjectMapper objectMapper = new ObjectMapper();
        Long mId = null;

        try {
            mId = objectMapper.readValue(message, Long.class);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Optional<List<Product>> optionalProducts = productRepository.findByMemberId(mId);
        if(optionalProducts.isPresent()) {

            productRepository.deleteAllByMemberIdIn(

                    optionalProducts.get()
                            .stream()
                            .map(Product::getMemberId)
                            .toList()
            );


            kafkaTemplate.send("canceled-orders-topic",

                    optionalProducts.get()
                            .stream()
                            .map(Product::getId)
                            .toList()
            );
        }
    }



    @KafkaListener(topics = "revert-stock-topic", containerFactory = "kafkaListener")
    public void revertStockQuantity(String message) {
        System.out.println("<<< ProductService - revertStockQuantity >>>");

        ObjectMapper objectMapper = new ObjectMapper();
        List<ProductUpdateStockDTO> dtoList = null;

        try {
            dtoList = objectMapper.readValue(message, new TypeReference<List<ProductUpdateStockDTO>>() {});

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        List<Product> productList = productRepository.findAllById(
                dtoList.stream()
                        .map(dto -> dto.getProductId())
                        .toList()
        );

        List<Product> updateProducts = new ArrayList<>();
        for(ProductUpdateStockDTO dto : dtoList) {
            for(Product p : productList) {
                if(dto.getProductId().equals(p.getId())) {
                    updateProducts.add(
                            Product.builder()
                                    .id(p.getId())
                                    .category(p.getCategory())
                                    .name(p.getName())
                                    .memberId(p.getMemberId())
                                    .price(p.getPrice())
                                    .stockQuantity(dto.getProductQuantity() + p.getStockQuantity())
                                    .build()
                    );
                }
            }
        }

        productRepository.saveAll(updateProducts);
    }
}
