package com.example.api.service;

import com.example.api.model.dto.OrderDetailDTO;
import com.example.api.model.entity.Order;
import com.example.api.model.entity.TrackRecord;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderService {

    /**
     * 创建订单
     */
    Order createOrder(String userId, Order order) throws Exception;

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

    /**
     * 分页获取订单列表（支持状态筛选和关键词搜索）
     */
    Page<Order> getOrdersPage(String userId, Integer status, String keyword, int page, int size);

    /**
     * 获取订单详情（含派车、车辆、司机信息）
     */
    OrderDetailDTO getOrderDetail(String userId, String orderId) throws Exception;

    /**
     * 获取订单轨迹记录
     */
    List<TrackRecord> getOrderTracking(String userId, String orderId) throws Exception;

    /**
     * 用户取消订单（带原因）
     * 仅允许 NEW(0) 和 APPROVED(1) 状态且未派车的订单
     * 
     * @param userId  用户ID
     * @param orderId 订单ID
     * @param reason  取消原因
     * @return 更新后的订单
     * @throws Exception 状态不允许取消时抛出异常
     */
    Order cancelOrderWithReason(String userId, String orderId, String reason) throws Exception;

    /**
     * 改约提货时间
     * 仅允许 NEW(0) 和 APPROVED(1) 状态且未派车的订单
     * 
     * @param userId             用户ID
     * @param orderId            订单ID
     * @param expectedPickupTime 新的提货时间
     * @param remark             备注
     * @return 更新后的订单
     * @throws Exception 状态不允许修改时抛出异常
     */
    Order rescheduleOrder(String userId, String orderId, String expectedPickupTime, String remark) throws Exception;

    // ========== 管理端方法 ==========

    /**
     * 管理端分页查询所有订单
     */
    Page<Order> findAllOrdersPage(Integer status, String keyword, int page, int size);

    /**
     * 管理端获取订单详情
     */
    OrderDetailDTO getOrderDetailAdmin(String orderId) throws Exception;

    /**
     * 审核通过订单
     */
    Order approveOrder(String orderId) throws Exception;

    /**
     * 驳回订单
     */
    Order rejectOrder(String orderId, String reason) throws Exception;

    /**
     * 更新订单状态（通用方法）
     */
    Order updateOrderStatus(String orderId, Integer status, String remark) throws Exception;

    /**
     * 获取各状态订单数量统计
     */
    java.util.Map<String, Long> getOrderStatusCounts();

    // ========== 用户仪表盘方法 ==========

    /**
     * 获取用户订单统计数据
     */
    java.util.Map<String, Object> getUserStats(String userId);

    /**
     * 获取用户运输中的订单（含物流信息）
     */
    java.util.List<java.util.Map<String, Object>> getInTransitOrders(String userId);

    /**
     * 获取用户消费分析数据
     */
    java.util.Map<String, Object> getExpenseAnalysis(String userId);

    /**
     * 获取用户最近账单
     */
    java.util.List<java.util.Map<String, Object>> getRecentBills(String userId);

    /**
     * 获取用户运输报告统计数据
     */
    java.util.Map<String, Object> getTransportReport(String userId);

}
