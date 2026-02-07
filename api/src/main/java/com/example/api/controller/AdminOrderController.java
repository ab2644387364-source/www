package com.example.api.controller;

import com.example.api.model.dto.OrderDetailDTO;
import com.example.api.model.entity.Order;
import com.example.api.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 管理端订单控制器
 * 提供订单审核、状态管理等功能
 */
@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    @Resource
    private OrderService orderService;

    /**
     * 分页获取所有订单
     * GET /api/admin/orders?status=1&keyword=xxx&page=0&size=10
     */
    @GetMapping("")
    public Page<Order> getOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return orderService.findAllOrdersPage(status, keyword, page, size);
    }

    /**
     * 获取订单详情
     * GET /api/admin/orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderDetail(@PathVariable String id) {
        try {
            OrderDetailDTO detail = orderService.getOrderDetailAdmin(id);
            return ResponseEntity.ok(detail);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * 审核通过订单
     * PUT /api/admin/orders/{id}/approve
     */
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveOrder(@PathVariable String id) {
        try {
            Order order = orderService.approveOrder(id);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * 驳回订单
     * PUT /api/admin/orders/{id}/reject
     * body: { "reason": "驳回原因" }
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectOrder(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        try {
            String reason = body.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                reason = "未提供原因";
            }
            Order order = orderService.rejectOrder(id, reason);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * 更新订单状态
     * PUT /api/admin/orders/{id}/status
     * body: { "status": 3, "remark": "备注" }
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable String id,
            @RequestBody Map<String, Object> body) {
        try {
            Integer status = (Integer) body.get("status");
            String remark = (String) body.get("remark");
            if (status == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", false,
                        "message", "状态不能为空"));
            }
            Order order = orderService.updateOrderStatus(id, status, remark);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * 获取订单状态统计
     * GET /api/admin/orders/stats
     */
    @GetMapping("/stats")
    public Map<String, Long> getOrderStats() {
        return orderService.getOrderStatusCounts();
    }
}
