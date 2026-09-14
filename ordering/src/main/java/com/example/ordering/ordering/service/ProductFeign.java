package com.example.ordering.ordering.service;

import com.example.ordering.ordering.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductFeign {

    @GetMapping("/product/{pId}")
    ProductDTO selectByProductId(@PathVariable Long pId);

    // @PutMapping("/product/updatestock")
    // void updateStock(@RequestBody ProductUpdateStockDTO dto);
}
