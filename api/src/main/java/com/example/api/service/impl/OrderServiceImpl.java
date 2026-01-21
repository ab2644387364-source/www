package com.example.api.service.impl;

import com.example.api.model.entity.Distribution;
import com.example.api.model.entity.Order;
import com.example.api.repository.DistributionRepository;
import com.example.api.repository.OrderRepository;
import com.example.api.service.NotificationService;
import com.example.api.service.OrderService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private OrderRepository orderRepository;

    @Resource
    private DistributionRepository distributionRepository;

    @Resource
    private NotificationService notificationService;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public Order createOrder(String userId, Order order) {
        order.setUserId(userId);
        order.setOrderNo(generateOrderNo());
        order.setAmount(calculateAmount(order));
        order.setStatus(0); // 待支付
        order.setCreateAt(dateFormat.format(new Date()));
        order.setUpdateAt(order.getCreateAt());
        Order saved = orderRepository.save(order);
        ensureDistribution(saved);
        return saved;
    }

    @Override
    public List<Order> getOrdersByUser(String userId) {
        return orderRepository.findByUserIdOrderByCreateAtDesc(userId);
    }

    @Override
    public List<Order> getPayableOrders(String userId) {
        return orderRepository.findByUserIdAndStatusOrderByCreateAtDesc(userId, 0);
    }

    @Override
    public List<Order> getRecentOrders(String userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreateAtDesc(userId);
        // 返回最近5条订单
        return orders.size() > 5 ? orders.subList(0, 5) : orders;
    }

    @Override
    public Order payOrder(String userId, String orderId, String paymentMethod) throws Exception {
        Order order = orderRepository.findByIdAndUserId(orderId, userId);
        if (order == null) {
            throw new Exception("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new Exception("订单状态不正确，无法支付");
        }
        order.setStatus(1); // 已支付
        order.setPaymentMethod(paymentMethod);
        order.setPaidAt(dateFormat.format(new Date()));
        order.setUpdateAt(order.getPaidAt());
        Order savedOrder = orderRepository.save(order);

        // 创建支付成功通知给管理员
        String title = "新订单支付通知";
        String content = String.format("用户 %s 已支付订单 %s，金额 ¥%.2f，支付方式：%s",
                userId, order.getOrderNo(), order.getAmount(), paymentMethod);
        notificationService.createSystemNotification(title, content);

        return savedOrder;
    }

    @Override
    public Order cancelOrder(String userId, String orderId) throws Exception {
        Order order = orderRepository.findByIdAndUserId(orderId, userId);
        if (order == null) {
            throw new Exception("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new Exception("只能取消待支付订单");
        }
        order.setStatus(-1); // 已取消
        order.setUpdateAt(dateFormat.format(new Date()));
        return orderRepository.save(order);
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = sdf.format(new Date());
        int random = (int) (Math.random() * 1000);
        return "OD" + timestamp + String.format("%03d", random);
    }

    /**
     * 计算订单金额（根据货物类型和数量）
     */
    private Double calculateAmount(Order order) {
        // 基础运费
        double basePrice = 200.0;
        // 单价（根据货物类型）
        double unitPrice;
        switch (order.getType()) {
            case "牛":
                unitPrice = 150.0;
                break;
            case "羊":
                unitPrice = 80.0;
                break;
            case "猪":
                unitPrice = 100.0;
                break;
            default:
                unitPrice = 100.0;
        }
        return basePrice + unitPrice * order.getQuantity();
    }

    private void ensureDistribution(Order order) {
        if (order == null || order.getOrderNo() == null || order.getOrderNo().trim().isEmpty()) {
            return;
        }
        Distribution existing = distributionRepository.findByOrderNo(order.getOrderNo());
        if (existing != null) {
            return;
        }
        Distribution distribution = new Distribution();
        distribution.setOrderNo(order.getOrderNo());
        distribution.setOrigin(order.getOrigin());
        distribution.setDestination(order.getDestination());
        distribution.setCurrentNode(order.getOrigin());
        distribution.setStatus(0);
        distribution.setWarningLevel(0);
        distribution.setWarningNote("");
        distribution.setUrgent(false);
        distribution.setCare(order.getRemark());
        String time = order.getPickupDate();
        if (time == null || time.trim().isEmpty()) {
            time = order.getCreateAt();
        }
        distribution.setTime(time);
        distributionRepository.save(distribution);
    }

}
