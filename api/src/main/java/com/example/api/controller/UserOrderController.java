package com.example.api.controller;

import com.example.api.model.entity.Order;
import com.example.api.service.OrderService;
import com.example.api.utils.JwtTokenUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 用户订单操作控制器
 * 提供用户端订单取消和改约功能
 */
@RestController
@RequestMapping("/api/user/orders")
public class UserOrderController {

    @Resource
    private OrderService orderService;

    /**
     * 取消订单
     * POST /api/user/orders/{id}/cancel
     * body: { "reason": "取消原因" }
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(
            @RequestHeader("Authorization") String token,
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        try {
            String userId = getUserIdFromToken(token);
            String reason = body.get("reason");
            Order order = orderService.cancelOrderWithReason(userId, id, reason);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * 改约提货时间
     * POST /api/user/orders/{id}/reschedule
     * body: { "expectedPickupTime": "2024-01-25 10:00", "remark": "备注" }
     */
    @PostMapping("/{id}/reschedule")
    public ResponseEntity<?> rescheduleOrder(
            @RequestHeader("Authorization") String token,
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        try {
            String userId = getUserIdFromToken(token);
            String expectedPickupTime = body.get("expectedPickupTime");
            String remark = body.get("remark");
            Order order = orderService.rescheduleOrder(userId, id, expectedPickupTime, remark);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", false,
                    "message", e.getMessage()));
        }
    }

    private String getUserIdFromToken(String token) throws Exception {
        String email = JwtTokenUtil.getUsername(token.replace("Bearer ", ""));
        return email;
    }
}
