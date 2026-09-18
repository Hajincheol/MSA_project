package com.example.product.product.domain;

import com.example.product.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Product extends BaseTimeEntity {

    // product id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // product명
    @Column(nullable = false)
    private String name;

    // 분류
    @Column(nullable = false)
    private String category;

    // 가격
    @Column(nullable = false)
    private Integer price;

    // 수량
    @Column(nullable = false)
    private Integer stockQuantity;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProductStatus productStatus = ProductStatus.AVAILABLE;

    // 회원정보
    // JPA에서 다대일(N:1) 관계를 매핑할 때 사용하는 어노테이션이다.
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    public void updateStockQuantity(int stockQuantity) {
        System.out.println("<<< Product - updateStockQuantity >>>");

        this.stockQuantity = this.stockQuantity - stockQuantity;
    }
}
