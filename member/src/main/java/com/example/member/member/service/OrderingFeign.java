package com.example.member.member.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ordering-service")
public interface OrderingFeign {

    @GetMapping("/ordering/existsByMemberId/{mId}")
    Boolean existsOrderingByMemberId(@PathVariable Long mId);
}
