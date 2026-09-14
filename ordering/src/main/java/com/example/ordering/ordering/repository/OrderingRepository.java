package com.example.ordering.ordering.repository;

import com.example.ordering.ordering.domain.OrderStatus;
import com.example.ordering.ordering.domain.Ordering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderingRepository extends JpaRepository<Ordering, Long> {
    Optional<List<Ordering>> findByMemberId(Long mId);

    Boolean existsByMemberIdAndOrderStatusEquals(Long mId, OrderStatus orderStatus);

    void deleteByMemberId(Long mId);

    Optional<List<Ordering>> findByProductIdIn(List<Long> productIdList);
}
