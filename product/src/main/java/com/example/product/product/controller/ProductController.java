package com.example.product.product.controller;

import com.example.product.product.dto.ProductRegisterDTO;
import com.example.product.product.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/product")
public class ProductController {

    // 필드
    private final ProductService productService;

    // 매개변수 생성자
    // RequiredArgsConstructor로 대체 가능
    public ProductController(ProductService productService) {
        System.out.println("<<< ProductController - 생성자 >>>");

        this.productService = productService;
    }

    // 등록 ----------------------------------------------------------------------
    // 등록 및 수정
    @PostMapping("/save")
    public ResponseEntity<?> productSave(@RequestBody ProductRegisterDTO dto, @RequestHeader("X-User-Id") String memberId) {
        System.out.println("<<< ProductController - productSave >>>");

        productService.productSave(dto, memberId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    // 조회 ----------------------------------------------------------------------
    // 제품 전체 목록
    // 단, 재고 수량이 0개 이하인 것들은 목록에서 제외
    @PostMapping("/list")
    public ResponseEntity<?> productList() {
        System.out.println("<<< ProductController - productList >>>");

        return new ResponseEntity<>(productService.productList(), HttpStatus.OK);
    }


    // 제품 id로 상세 조회
    @PostMapping("/selectById/{pId}")
    public ResponseEntity<?> selectById(@PathVariable String pId) {
        System.out.println("<<< ProductController - selectById >>>");

        return new ResponseEntity<>(productService.selectById(Long.parseLong(pId)), HttpStatus.OK);
    }


    // 제품 이름으로 상세 조회
    @PostMapping("/selectByName/{pName}")
    public ResponseEntity<?> productSelectByName(@PathVariable String pName) {
        System.out.println("<<< ProductController - productSelectByName >>>");

        return new ResponseEntity<>(productService.selectByName(pName), HttpStatus.OK);
    }


    // 특정 유저의 id로 검색
    @PostMapping("/selectByMemberId/{mId}")
    public ResponseEntity<?> selectByMemberId(@PathVariable String mId) {
        System.out.println("<<< ProductController - selectByMemberId >>>");

        return new ResponseEntity<>(productService.selectByMemberId(mId), HttpStatus.OK);
    }

    // openFeign
    // 지정 제품 재고 조회 API
    @GetMapping("{id}")
    public ResponseEntity<?> productDetail(@PathVariable Long id) {
        System.out.println("<<< ProductController - productDetail >>>");

        // 3초지연
        // Thread.sleep(3000L);

        return new ResponseEntity<>(productService.selectById(id), HttpStatus.OK);
    }


    // 해당 제품으로 주문된 모든 주문 list 조회
    @PostMapping("/orderingProductList")
    public ResponseEntity<?> orderingProductList(@RequestBody Long[] pId) {
        System.out.println("<<< ProductController - orderingProductList >>>");

        return new ResponseEntity<>(productService.orderingProductList(Arrays.asList(pId)), HttpStatus.OK);
    }


    // openFeign 해당 member가 등록한 제품이 존재하는지 확인
    @GetMapping("/existsByMemberId/{mId}")
    public ResponseEntity<?> existsByMemberId(@PathVariable Long mId) {
        System.out.println("<<< ProductController - existsByMemberId >>>");

        return new ResponseEntity<>(productService.existsByMemberId(mId), HttpStatus.OK);
    }


    // 상태 ----------------------------------------------------------------------
    // 제품 비활성화
    @PostMapping("/changeStatus/{pId}")
    public ResponseEntity<?> productDelete(@PathVariable String pId) {
        System.out.println("<<< ProductController - changeStatus >>>");

        productService.changeStatus(pId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /*
    // openFeign
    @PutMapping("/updatestock")
    public ResponseEntity<?> updateStock(@RequestBody ProductUpdateStockDTO productUpdateStockDTO) {
        System.out.println("<<< ProductController - updateStock >>>");

        Product product = productService.updateStockQuantity(productUpdateStockDTO);
        return new ResponseEntity<>(product.getId(), HttpStatus.OK);
    }
    */
}
