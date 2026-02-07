package com.example.api.repository;

import com.example.api.model.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {

    /**
     * 根据订单ID查找评价
     */
    Optional<Review> findByOrderId(String orderId);

    /**
     * 检查用户是否已对该订单评价
     */
    boolean existsByOrderIdAndUserId(String orderId, String userId);

    /**
     * 检查订单是否已被评价
     */
    boolean existsByOrderId(String orderId);

}
