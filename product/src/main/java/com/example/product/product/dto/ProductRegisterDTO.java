package com.example.product.product.dto;

import com.example.product.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProductRegisterDTO {

    private Long id;

    // 제품명
    private String name;

    // 제품 카테고리
    private String category;

    // 제품 가격
    private int price;

    // 제품 수량
    private int stockQuantity;

    public Product toEntity(Long memberId) {

        return Product.builder()
                .id(this.id)
                .name(this.name)
                .category(this.category)
                .price(this.price)
                .stockQuantity(this.stockQuantity)
                .memberId(memberId)
                .build();
    }
}
