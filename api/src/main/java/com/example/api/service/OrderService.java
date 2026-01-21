package com.example.api.service;

import com.example.api.model.entity.Order;

import java.util.List;

public interface OrderService {

    /**
     * 创建订单
     */
    Order createOrder(String userId, Order order);

    /**
     * 获取用户所有订单
     */
    List<Order> getOrdersByUser(String userId);

    /**
     * 获取用户待支付订单
     */
    List<Order> getPayableOrders(String userId);

    /**
     * 获取用户最近订单（用于首页展示）
     */
    List<Order> getRecentOrders(String userId);

    /**
     * 支付订单
     */
    Order payOrder(String userId, String orderId, String paymentMethod) throws Exception;

    /**
     * 取消订单
     */
    Order cancelOrder(String userId, String orderId) throws Exception;

}
