package com.example.member.member.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductFeign {

    @GetMapping("/product/existsByMemberId/{mId}")
    Boolean existsProductByMemberId(@PathVariable Long mId);
}
