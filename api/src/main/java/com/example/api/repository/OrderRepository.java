package com.example.api.repository;

import com.example.api.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findByUserIdOrderByCreateAtDesc(String userId);

    List<Order> findByUserIdAndStatusOrderByCreateAtDesc(String userId, Integer status);

    Order findByOrderNo(String orderNo);

    Order findByIdAndUserId(String id, String userId);

}
