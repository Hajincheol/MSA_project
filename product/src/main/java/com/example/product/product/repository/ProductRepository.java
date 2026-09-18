package com.example.product.product.repository;

import com.example.product.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByMemberId(long mId);

    List<Product> findAllByMemberIdAndStockQuantityGreaterThan(Long mId, Integer stockQuantity);

    List<Product> findAllByIdInOrderById(Iterable<Long> ids);

    Boolean existsByMemberId(Long mId);

    List<Product> findAllByNameContaining(String pName);

    List<Product> findAllByIdIn(List<Long> list);
}
