package com.example.product.product.repository;

import com.example.product.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByMemberId(long mId);

    List<Product> findByMemberIdAndStockQuantityGreaterThan(Long mId, Integer stockQuantity);

    List<Product> findAllByIdInOrderById(Iterable<Long> ids);

    Boolean existsByMemberId(Long mId);

    void deleteAllByMemberIdIn(List<Long> productList);

    List<Product> findByNameContaining(String pName);
}
