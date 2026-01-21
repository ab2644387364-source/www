package com.example.api.controller;

import com.example.api.model.entity.Order;
import com.example.api.service.OrderService;
import com.example.api.utils.JwtTokenUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Resource
    private OrderService orderService;

    /**
     * 创建订单
     */
    @PostMapping("/create")
    public Order createOrder(
            @RequestHeader("Authorization") String token,
            @RequestBody Order order) throws Exception {
        String userId = getUserIdFromToken(token);
        return orderService.createOrder(userId, order);
    }

    /**
     * 获取用户所有订单
     */
    @GetMapping("/list")
    public List<Order> getOrders(@RequestHeader("Authorization") String token) throws Exception {
        String userId = getUserIdFromToken(token);
        return orderService.getOrdersByUser(userId);
    }

    /**
     * 获取待支付订单
     */
    @GetMapping("/payable")
    public List<Order> getPayableOrders(@RequestHeader("Authorization") String token) throws Exception {
        String userId = getUserIdFromToken(token);
        return orderService.getPayableOrders(userId);
    }

    /**
     * 获取最近订单
     */
    @GetMapping("/recent")
    public List<Order> getRecentOrders(@RequestHeader("Authorization") String token) throws Exception {
        String userId = getUserIdFromToken(token);
        return orderService.getRecentOrders(userId);
    }

    /**
     * 支付订单
     */
    @PostMapping("/pay")
    public Order payOrder(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> body) throws Exception {
        String userId = getUserIdFromToken(token);
        String orderId = body.get("orderId");
        String paymentMethod = body.get("method");
        return orderService.payOrder(userId, orderId, paymentMethod);
    }

    /**
     * 取消订单
     */
    @PostMapping("/cancel")
    public Order cancelOrder(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> body) throws Exception {
        String userId = getUserIdFromToken(token);
        String orderId = body.get("orderId");
        return orderService.cancelOrder(userId, orderId);
    }

    private String getUserIdFromToken(String token) throws Exception {
        String email = JwtTokenUtil.getUsername(token.replace("Bearer ", ""));
        // 这里返回 email 作为 userId，因为 User 表中 email 是唯一标识
        return email;
    }

}
