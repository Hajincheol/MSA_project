package com.example.ordering.ordering.controller;

import com.example.ordering.ordering.domain.Ordering;
import com.example.ordering.ordering.dto.OrderCreateDTO;
import com.example.ordering.ordering.service.OrderingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ordering")
public class OrderingController {

    private final OrderingService orderingService;

    public OrderingController(OrderingService orderingService) {
        this.orderingService = orderingService;
    }

    // 매개변수 추가
    // => @RequestHeader("X-User-Id") String userId
    // => ApiGateway에서 헤더로 넘긴 검증받은 X-User-Id임
    @PostMapping("/create")
    public ResponseEntity<?> openFeignKafkaOrderCreate(@RequestBody OrderCreateDTO dto, @RequestHeader("X-User-Id") String userid) {
        System.out.println("<<< OrderingController - orderCreate >>>");

        Ordering ordering = orderingService.openFeignKafkaOrderCreate(dto, userid);
        return new ResponseEntity<>(ordering.getId(), HttpStatus.CREATED);
    }

    // 특정 유저의 id로 검색
    @PostMapping("/selectByMemberId/{mId}")
    public ResponseEntity<?> selectByMemberId(@PathVariable String mId) {
        System.out.println("<<< OrderingController - selectByMemberId >>>");

        return new ResponseEntity<>(orderingService.selectByMemberId(mId), HttpStatus.OK);
    }

    // 주문 취소
    @PostMapping("/cancel/{oId}")
    public ResponseEntity<?> orderCancel(@PathVariable String oId) {
        System.out.println("<<< OrderingController - orderCancel >>>");

        orderingService.orderCancel(oId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/existsByMemberId/{mId}")
    public ResponseEntity<?> existsByMemberId(@PathVariable Long mId) {
        System.out.println("<<< OrderingController - existsByMemberId >>>");

        return new ResponseEntity<>(orderingService.existsByMemberId(mId), HttpStatus.OK);
    }
}
