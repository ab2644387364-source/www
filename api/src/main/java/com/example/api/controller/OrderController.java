package com.example.api.controller;

import com.example.api.model.dto.OrderDetailDTO;
import com.example.api.model.entity.Order;
import com.example.api.model.entity.TrackRecord;
import com.example.api.service.OrderService;
import com.example.api.utils.JwtTokenUtil;
import org.springframework.data.domain.Page;
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

    /**
     * 分页获取订单列表
     * GET /api/order/page?status=1&keyword=xxx&page=0&size=10
     */
    @GetMapping("/page")
    public Page<Order> getOrdersPage(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws Exception {
        String userId = getUserIdFromToken(token);
        return orderService.getOrdersPage(userId, status, keyword, page, size);
    }

    /**
     * 获取订单详情
     * GET /api/order/{id}/detail
     */
    @GetMapping("/{id}/detail")
    public OrderDetailDTO getOrderDetail(
            @RequestHeader("Authorization") String token,
            @PathVariable String id) throws Exception {
        String userId = getUserIdFromToken(token);
        return orderService.getOrderDetail(userId, id);
    }

    /**
     * 获取订单轨迹记录
     * GET /api/order/{id}/tracking
     */
    @GetMapping("/{id}/tracking")
    public List<TrackRecord> getOrderTracking(
            @RequestHeader("Authorization") String token,
            @PathVariable String id) throws Exception {
        String userId = getUserIdFromToken(token);
        return orderService.getOrderTracking(userId, id);
    }

    // ========== 用户仪表盘接口 ==========

    /**
     * 获取用户订单统计数据
     * GET /api/order/user-stats
     */
    @GetMapping("/user-stats")
    public Map<String, Object> getUserStats(@RequestHeader("Authorization") String token) throws Exception {
        String userId = getUserIdFromToken(token);
        return orderService.getUserStats(userId);
    }

    /**
     * 获取用户运输中的订单
     * GET /api/order/in-transit
     */
    @GetMapping("/in-transit")
    public List<Map<String, Object>> getInTransitOrders(@RequestHeader("Authorization") String token) throws Exception {
        String userId = getUserIdFromToken(token);
        return orderService.getInTransitOrders(userId);
    }

    private String getUserIdFromToken(String token) throws Exception {
        String email = JwtTokenUtil.getUsername(token.replace("Bearer ", ""));
        // 这里返回 email 作为 userId，因为 User 表中 email 是唯一标识
        return email;
    }

    /**
     * 获取用户消费分析数据
     * GET /api/order/expense-analysis
     */
    @GetMapping("/expense-analysis")
    public Map<String, Object> getExpenseAnalysis(@RequestHeader("Authorization") String token) throws Exception {
        String userId = getUserIdFromToken(token);
        return orderService.getExpenseAnalysis(userId);
    }

    /**
     * 获取用户最近账单
     * GET /api/order/recent-bills
     */
    @GetMapping("/recent-bills")
    public List<Map<String, Object>> getRecentBills(@RequestHeader("Authorization") String token) throws Exception {
        String userId = getUserIdFromToken(token);
        return orderService.getRecentBills(userId);
    }

    /**
     * 获取用户运输报告统计数据
     * GET /api/order/transport-report
     */
    @GetMapping("/transport-report")
    public Map<String, Object> getTransportReport(@RequestHeader("Authorization") String token) throws Exception {
        String userId = getUserIdFromToken(token);
        return orderService.getTransportReport(userId);
    }

}
