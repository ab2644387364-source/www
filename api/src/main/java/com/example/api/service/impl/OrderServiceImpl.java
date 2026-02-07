package com.example.api.service.impl;

import com.example.api.model.dto.OrderDetailDTO;
import com.example.api.model.entity.*;
import com.example.api.repository.*;
import com.example.api.service.NotificationService;
import com.example.api.service.OrderService;
import com.example.api.service.TrackRecordService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private OrderRepository orderRepository;

    @Resource
    private DistributionRepository distributionRepository;

    @Resource
    private VehicleRepository vehicleRepository;

    @Resource
    private DriverRepository driverRepository;

    @Resource
    private TrackRecordService trackRecordService;

    @Resource
    private NotificationService notificationService;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public Order createOrder(String userId, Order order) throws Exception {
        // 验证手机号 - 必须是11位且以1开头
        String phone = order.getFromPhone();
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new Exception("请输入正确的11位手机号");
        }

        order.setUserId(userId);
        order.setOrderNo(generateOrderNo());
        order.setAmount(calculateAmount(order));
        order.setStatus(0); // 待支付
        order.setCreateAt(dateFormat.format(new Date()));
        order.setUpdateAt(order.getCreateAt());
        Order saved = orderRepository.save(order);
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

        // 支付成功后创建配送单，关联订单号
        ensureDistribution(savedOrder);

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

    @Override
    public Page<Order> getOrdersPage(String userId, Integer status, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasStatus = status != null;

        if (hasKeyword && hasStatus) {
            return orderRepository.searchByKeywordAndStatus(userId, keyword.trim(), status, pageable);
        } else if (hasKeyword) {
            return orderRepository.searchByKeyword(userId, keyword.trim(), pageable);
        } else if (hasStatus) {
            return orderRepository.findByUserIdAndStatusOrderByCreateAtDesc(userId, status, pageable);
        } else {
            return orderRepository.findByUserIdOrderByCreateAtDesc(userId, pageable);
        }
    }

    @Override
    public OrderDetailDTO getOrderDetail(String userId, String orderId) throws Exception {
        Order order = orderRepository.findByIdAndUserId(orderId, userId);
        if (order == null) {
            throw new Exception("订单不存在");
        }

        OrderDetailDTO dto = new OrderDetailDTO(order);

        // 查询关联的配送单
        Distribution distribution = distributionRepository.findByOrderNo(order.getOrderNo());
        if (distribution != null) {
            dto.setDistribution(distribution);

            // 查询车辆信息
            if (distribution.getVid() != null && !distribution.getVid().isEmpty()) {
                vehicleRepository.findById(distribution.getVid()).ifPresent(dto::setVehicle);
            }

            // 查询司机信息
            if (distribution.getDid() != null && !distribution.getDid().isEmpty()) {
                driverRepository.findById(distribution.getDid()).ifPresent(dto::setDriver);
            }

            // 查询轨迹记录
            List<TrackRecord> trackRecords = trackRecordService.findByDistributionId(distribution.getId());
            dto.setTrackRecords(trackRecords != null ? trackRecords : new ArrayList<>());
        } else {
            dto.setTrackRecords(new ArrayList<>());
        }

        return dto;
    }

    @Override
    public List<TrackRecord> getOrderTracking(String userId, String orderId) throws Exception {
        Order order = orderRepository.findByIdAndUserId(orderId, userId);
        if (order == null) {
            throw new Exception("订单不存在");
        }

        Distribution distribution = distributionRepository.findByOrderNo(order.getOrderNo());
        if (distribution == null) {
            return new ArrayList<>();
        }

        List<TrackRecord> records = trackRecordService.findByDistributionId(distribution.getId());
        return records != null ? records : new ArrayList<>();
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
        Distribution savedDistribution = distributionRepository.save(distribution);

        // 自动创建初始物流轨迹记录
        TrackRecord initialTrack = new TrackRecord();
        initialTrack.setDistributionId(savedDistribution.getId());
        initialTrack.setNode(order.getOrigin());
        initialTrack.setStatus("下单");
        initialTrack.setRemark("订单已支付，等待发货");
        initialTrack.setOperator("系统");
        trackRecordService.addRecord(initialTrack);
    }

    @Override
    public Order cancelOrderWithReason(String userId, String orderId, String reason) throws Exception {
        Order order = orderRepository.findByIdAndUserId(orderId, userId);
        if (order == null) {
            throw new Exception("订单不存在");
        }

        // 状态校验：只允许 NEW(0) 和 APPROVED(1)
        if (order.getStatus() != 0 && order.getStatus() != 1) {
            throw new Exception("当前订单状态不允许取消");
        }

        // 派车校验：已分配司机/车辆的订单不能取消
        Distribution dist = distributionRepository.findByOrderNo(order.getOrderNo());
        if (dist != null && isDistributed(dist)) {
            throw new Exception("订单已派车，无法取消");
        }

        // 更新订单
        order.setStatus(-1);
        String originalRemark = order.getRemark() != null ? order.getRemark() : "";
        order.setRemark(originalRemark + " [取消原因: " + reason + "]");
        order.setUpdateAt(dateFormat.format(new Date()));

        return orderRepository.save(order);
    }

    @Override
    public Order rescheduleOrder(String userId, String orderId, String expectedPickupTime, String remark)
            throws Exception {
        Order order = orderRepository.findByIdAndUserId(orderId, userId);
        if (order == null) {
            throw new Exception("订单不存在");
        }

        // 状态校验
        if (order.getStatus() != 0 && order.getStatus() != 1) {
            throw new Exception("当前订单状态不允许修改提货时间");
        }

        // 派车校验：已分配司机/车辆的订单不能修改
        Distribution dist = distributionRepository.findByOrderNo(order.getOrderNo());
        if (dist != null && isDistributed(dist)) {
            throw new Exception("订单已派车，无法修改提货时间");
        }

        // 更新订单
        order.setPickupDate(expectedPickupTime);
        if (remark != null && !remark.isEmpty()) {
            String originalRemark = order.getRemark() != null ? order.getRemark() : "";
            order.setRemark(originalRemark + " [改约备注: " + remark + "]");
        }
        order.setUpdateAt(dateFormat.format(new Date()));

        return orderRepository.save(order);
    }

    /**
     * 判断配送单是否已分配司机/车辆
     * 已分配的条件：status >= 1 或者 已指定司机(did) 或 已指定车辆(vid)
     */
    private boolean isDistributed(Distribution dist) {
        if (dist == null) {
            return false;
        }
        // 状态>=1 表示已开始处理
        if (dist.getStatus() != null && dist.getStatus() >= 1) {
            return true;
        }
        // 已分配司机
        if (dist.getDid() != null && !dist.getDid().trim().isEmpty()) {
            return true;
        }
        // 已分配车辆
        if (dist.getVid() != null && !dist.getVid().trim().isEmpty()) {
            return true;
        }
        return false;
    }

    // ========== 管理端方法实现 ==========

    @Override
    public Page<Order> findAllOrdersPage(Integer status, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasStatus = status != null;

        if (hasKeyword && hasStatus) {
            return orderRepository.searchByKeywordAndStatusForAdmin(keyword.trim(), status, pageable);
        } else if (hasKeyword) {
            return orderRepository.searchByKeywordForAdmin(keyword.trim(), pageable);
        } else if (hasStatus) {
            return orderRepository.findByStatusOrderByCreateAtDesc(status, pageable);
        } else {
            return orderRepository.findAllByOrderByCreateAtDesc(pageable);
        }
    }

    @Override
    public OrderDetailDTO getOrderDetailAdmin(String orderId) throws Exception {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            throw new Exception("订单不存在");
        }

        OrderDetailDTO dto = new OrderDetailDTO(order);

        // 查询关联的配送单
        Distribution distribution = distributionRepository.findByOrderNo(order.getOrderNo());
        if (distribution != null) {
            dto.setDistribution(distribution);

            // 查询车辆信息
            if (distribution.getVid() != null && !distribution.getVid().isEmpty()) {
                vehicleRepository.findById(distribution.getVid()).ifPresent(dto::setVehicle);
            }

            // 查询司机信息
            if (distribution.getDid() != null && !distribution.getDid().isEmpty()) {
                driverRepository.findById(distribution.getDid()).ifPresent(dto::setDriver);
            }

            // 查询轨迹记录
            List<TrackRecord> trackRecords = trackRecordService.findByDistributionId(distribution.getId());
            dto.setTrackRecords(trackRecords != null ? trackRecords : new ArrayList<>());
        } else {
            dto.setTrackRecords(new ArrayList<>());
        }

        return dto;
    }

    @Override
    public Order approveOrder(String orderId) throws Exception {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            throw new Exception("订单不存在");
        }
        if (order.getStatus() != 1) {
            throw new Exception("只能审核已支付的订单");
        }

        order.setStatus(2); // 已审核
        order.setUpdateAt(dateFormat.format(new Date()));
        Order savedOrder = orderRepository.save(order);

        // 发送通知给用户
        notificationService.createUserNotification(
                "订单审核通过",
                String.format("您的订单 %s 已审核通过，正在安排配送", order.getOrderNo()),
                order.getUserId(),
                null);

        return savedOrder;
    }

    @Override
    public Order rejectOrder(String orderId, String reason) throws Exception {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            throw new Exception("订单不存在");
        }
        if (order.getStatus() != 1) {
            throw new Exception("只能驳回已支付的订单");
        }

        order.setStatus(-1); // 已取消
        String originalRemark = order.getRemark() != null ? order.getRemark() : "";
        order.setRemark(originalRemark + " [驳回原因: " + reason + "]");
        order.setUpdateAt(dateFormat.format(new Date()));
        Order savedOrder = orderRepository.save(order);

        // 发送通知给用户
        notificationService.createUserNotification(
                "订单已驳回",
                String.format("您的订单 %s 已被驳回，原因：%s。订单金额将在3-5个工作日内退回", order.getOrderNo(), reason),
                order.getUserId(),
                null);

        return savedOrder;
    }

    @Override
    public Order updateOrderStatus(String orderId, Integer status, String remark) throws Exception {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            throw new Exception("订单不存在");
        }

        Integer oldStatus = order.getStatus();
        order.setStatus(status);
        if (remark != null && !remark.isEmpty()) {
            String originalRemark = order.getRemark() != null ? order.getRemark() : "";
            order.setRemark(originalRemark + " [备注: " + remark + "]");
        }
        order.setUpdateAt(dateFormat.format(new Date()));
        Order savedOrder = orderRepository.save(order);

        // 根据状态变更发送不同通知
        String title = "";
        String content = "";
        switch (status) {
            case 2:
                title = "订单已审核";
                content = String.format("您的订单 %s 已审核通过，即将安排配送", order.getOrderNo());
                break;
            case 3:
                title = "订单配送中";
                content = String.format("您的订单 %s 已发货，正在运输中", order.getOrderNo());
                break;
            case 4:
                title = "订单已送达";
                content = String.format("您的订单 %s 已送达目的地，请确认收货", order.getOrderNo());
                break;
            default:
                title = "订单状态更新";
                content = String.format("您的订单 %s 状态已更新", order.getOrderNo());
        }

        if (!title.isEmpty()) {
            notificationService.createUserNotification(title, content, order.getUserId(), null);
        }

        return savedOrder;
    }

    @Override
    public java.util.Map<String, Long> getOrderStatusCounts() {
        java.util.Map<String, Long> counts = new java.util.HashMap<>();
        counts.put("pending", orderRepository.countByStatus(0)); // 待支付
        counts.put("paid", orderRepository.countByStatus(1)); // 已支付/待审核
        counts.put("approved", orderRepository.countByStatus(2)); // 已审核
        counts.put("shipping", orderRepository.countByStatus(3)); // 运输中
        counts.put("completed", orderRepository.countByStatus(4)); // 已完成
        counts.put("cancelled", orderRepository.countByStatus(-1)); // 已取消
        counts.put("total", orderRepository.count()); // 总数
        return counts;
    }

    // ========== 用户仪表盘方法 ==========

    @Override
    public java.util.Map<String, Object> getUserStats(String userId) {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();

        // 订单数量统计
        stats.put("totalOrders", orderRepository.countByUserId(userId));
        stats.put("pendingPayment", orderRepository.countByUserIdAndStatus(userId, 0));
        stats.put("inTransit", orderRepository.countByUserIdAndStatus(userId, 2) +
                orderRepository.countByUserIdAndStatus(userId, 3));
        stats.put("completed", orderRepository.countByUserIdAndStatus(userId, 4));
        stats.put("cancelled", orderRepository.countByUserIdAndStatus(userId, -1));

        // 累计消费金额
        Double totalAmount = orderRepository.sumAmountByUserId(userId);
        stats.put("totalAmount", totalAmount != null ? totalAmount : 0.0);

        return stats;
    }

    @Override
    public java.util.List<java.util.Map<String, Object>> getInTransitOrders(String userId) {
        java.util.List<java.util.Map<String, Object>> result = new ArrayList<>();

        // 获取运输中的订单 (status = 2 审核通过待配送, status = 3 运输中)
        List<Order> ordersStatus2 = orderRepository.findByUserIdAndStatusOrderByCreateAtDesc(userId, 2);
        List<Order> ordersStatus3 = orderRepository.findByUserIdAndStatusOrderByCreateAtDesc(userId, 3);

        List<Order> inTransitOrders = new ArrayList<>();
        inTransitOrders.addAll(ordersStatus2);
        inTransitOrders.addAll(ordersStatus3);

        for (Order order : inTransitOrders) {
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("orderId", order.getId());
            item.put("orderNo", order.getOrderNo());
            item.put("origin", order.getOrigin());
            item.put("destination", order.getDestination());
            item.put("status", order.getStatus());
            item.put("createAt", order.getCreateAt());

            // 查找关联的配送单
            Distribution distribution = distributionRepository.findByOrderNo(order.getOrderNo());
            if (distribution != null) {
                item.put("currentNode", distribution.getCurrentNode());
                item.put("distributionStatus", distribution.getStatus());
                item.put("driver", distribution.getDriver());
            } else {
                item.put("currentNode", "等待调度");
                item.put("distributionStatus", 0);
                item.put("driver", null);
            }

            result.add(item);
        }

        return result;
    }

    @Override
    public java.util.Map<String, Object> getExpenseAnalysis(String userId) {
        java.util.Map<String, Object> analysis = new java.util.HashMap<>();

        // 获取用户所有已支付订单（status >= 1，排除取消的）
        List<Order> paidOrders = orderRepository.findByUserIdAndStatusGreaterThanEqual(userId, 1);
        paidOrders.removeIf(order -> order.getStatus() == -1);

        // 累计消费
        double totalExpense = 0;
        int orderCount = paidOrders.size();
        for (Order order : paidOrders) {
            if (order.getAmount() != null) {
                totalExpense += order.getAmount();
            }
        }
        analysis.put("totalExpense", totalExpense);
        analysis.put("orderCount", orderCount);
        analysis.put("avgOrderAmount", orderCount > 0 ? totalExpense / orderCount : 0);
        analysis.put("expenseTrend", 0); // 暂无趋势对比数据
        analysis.put("savings", 0); // 暂无优惠券系统
        analysis.put("couponUsed", 0);

        // 费用构成（简化版：全部作为运输费）
        java.util.List<java.util.Map<String, Object>> breakdown = new java.util.ArrayList<>();
        java.util.Map<String, Object> transport = new java.util.HashMap<>();
        transport.put("name", "运输费");
        transport.put("percent", 100);
        transport.put("amount", totalExpense);
        transport.put("color", "#1890ff");
        breakdown.add(transport);
        analysis.put("expenseBreakdown", breakdown);

        // 消费排行（按路线分组）
        java.util.Map<String, java.util.Map<String, Object>> routeMap = new java.util.HashMap<>();
        for (Order order : paidOrders) {
            String route = order.getOrigin() + " → " + order.getDestination();
            if (!routeMap.containsKey(route)) {
                java.util.Map<String, Object> routeInfo = new java.util.HashMap<>();
                routeInfo.put("route", route);
                routeInfo.put("count", 0);
                routeInfo.put("amount", 0.0);
                routeMap.put(route, routeInfo);
            }
            java.util.Map<String, Object> routeInfo = routeMap.get(route);
            routeInfo.put("count", (int) routeInfo.get("count") + 1);
            routeInfo.put("amount",
                    (double) routeInfo.get("amount") + (order.getAmount() != null ? order.getAmount() : 0));
        }

        // 按消费金额排序取前5
        java.util.List<java.util.Map<String, Object>> topRoutes = new java.util.ArrayList<>(routeMap.values());
        topRoutes.sort((a, b) -> Double.compare((double) b.get("amount"), (double) a.get("amount")));
        if (topRoutes.size() > 5) {
            topRoutes = topRoutes.subList(0, 5);
        }
        analysis.put("topExpenseRoutes", topRoutes);

        return analysis;
    }

    @Override
    public java.util.List<java.util.Map<String, Object>> getRecentBills(String userId) {
        java.util.List<java.util.Map<String, Object>> bills = new java.util.ArrayList<>();

        // 获取最近10条已支付订单
        List<Order> recentOrders = orderRepository.findByUserIdAndStatusGreaterThanEqual(userId, 1);
        recentOrders.removeIf(order -> order.getStatus() == -1);
        recentOrders.sort((a, b) -> b.getCreateAt().compareTo(a.getCreateAt()));
        if (recentOrders.size() > 10) {
            recentOrders = recentOrders.subList(0, 10);
        }

        for (Order order : recentOrders) {
            java.util.Map<String, Object> bill = new java.util.HashMap<>();
            bill.put("orderNo", order.getOrderNo());
            bill.put("route", order.getOrigin() + " → " + order.getDestination());
            bill.put("date", order.getPaidAt() != null ? order.getPaidAt().substring(0, 10)
                    : order.getCreateAt().substring(0, 10));
            bill.put("amount", order.getAmount());
            bills.add(bill);
        }

        return bills;
    }

    @Override
    public java.util.Map<String, Object> getTransportReport(String userId) {
        java.util.Map<String, Object> report = new java.util.HashMap<>();

        // 获取用户所有订单
        List<Order> allOrders = orderRepository.findByUserIdOrderByCreateAtDesc(userId);

        // 基础统计
        int totalOrders = allOrders.size();
        double totalAmount = 0;
        int totalQuantity = 0;

        for (Order order : allOrders) {
            if (order.getAmount() != null) {
                totalAmount += order.getAmount();
            }
            if (order.getQuantity() != null) {
                totalQuantity += order.getQuantity();
            }
        }

        report.put("totalOrders", totalOrders);
        report.put("totalAmount", totalAmount);
        report.put("totalLivestock", totalQuantity);
        report.put("totalDistance", totalOrders * 500); // 估算距离

        // 月度订单趋势（最近12个月）
        java.util.Map<String, Integer> monthlyOrders = new java.util.LinkedHashMap<>();
        java.util.Map<String, Double> monthlyAmount = new java.util.LinkedHashMap<>();

        // 获取最近12个月
        java.text.SimpleDateFormat monthFormat = new java.text.SimpleDateFormat("yyyy-MM");
        for (int i = 11; i >= 0; i--) {
            java.util.Calendar temp = java.util.Calendar.getInstance();
            temp.add(java.util.Calendar.MONTH, -i);
            String monthKey = monthFormat.format(temp.getTime());
            monthlyOrders.put(monthKey, 0);
            monthlyAmount.put(monthKey, 0.0);
        }

        // 统计每月订单数和金额
        for (Order order : allOrders) {
            if (order.getCreateAt() != null && order.getCreateAt().length() >= 7) {
                String orderMonth = order.getCreateAt().substring(0, 7);
                if (monthlyOrders.containsKey(orderMonth)) {
                    monthlyOrders.put(orderMonth, monthlyOrders.get(orderMonth) + 1);
                    double amt = order.getAmount() != null ? order.getAmount() : 0;
                    monthlyAmount.put(orderMonth, monthlyAmount.get(orderMonth) + amt);
                }
            }
        }

        // 转换为前端需要的格式
        java.util.List<java.util.Map<String, Object>> monthlyTrend = new java.util.ArrayList<>();
        for (String month : monthlyOrders.keySet()) {
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("month", month.substring(5) + "月"); // 只取月份
            item.put("orderCount", monthlyOrders.get(month));
            item.put("amount", monthlyAmount.get(month));
            monthlyTrend.add(item);
        }
        report.put("monthlyTrend", monthlyTrend);

        // 常用路线 TOP5
        java.util.Map<String, java.util.Map<String, Object>> routeMap = new java.util.HashMap<>();
        for (Order order : allOrders) {
            String routeKey = order.getOrigin() + " → " + order.getDestination();
            if (!routeMap.containsKey(routeKey)) {
                java.util.Map<String, Object> routeInfo = new java.util.HashMap<>();
                routeInfo.put("origin", order.getOrigin());
                routeInfo.put("destination", order.getDestination());
                routeInfo.put("count", 0);
                routeMap.put(routeKey, routeInfo);
            }
            java.util.Map<String, Object> routeInfo = routeMap.get(routeKey);
            routeInfo.put("count", (int) routeInfo.get("count") + 1);
        }

        java.util.List<java.util.Map<String, Object>> topRoutes = new java.util.ArrayList<>(routeMap.values());
        topRoutes.sort((a, b) -> (int) b.get("count") - (int) a.get("count"));
        if (topRoutes.size() > 5) {
            topRoutes = topRoutes.subList(0, 5);
        }

        // 计算百分比
        int maxCount = topRoutes.isEmpty() ? 1 : (int) topRoutes.get(0).get("count");
        for (java.util.Map<String, Object> route : topRoutes) {
            int count = (int) route.get("count");
            route.put("percent", maxCount > 0 ? (count * 100 / maxCount) : 0);
        }
        report.put("topRoutes", topRoutes);

        // 货物类型分布
        java.util.Map<String, Integer> typeCount = new java.util.HashMap<>();
        for (Order order : allOrders) {
            String type = order.getType() != null ? order.getType() : "其他";
            typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);
        }

        java.util.List<java.util.Map<String, Object>> typeDistribution = new java.util.ArrayList<>();
        String[] colors = { "#1890ff", "#52c41a", "#faad14", "#f5222d", "#722ed1" };
        int colorIndex = 0;
        for (String type : typeCount.keySet()) {
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("type", type);
            item.put("count", typeCount.get(type));
            item.put("percent", totalOrders > 0 ? typeCount.get(type) * 100 / totalOrders : 0);
            item.put("color", colors[colorIndex % colors.length]);
            typeDistribution.add(item);
            colorIndex++;
        }
        report.put("typeDistribution", typeDistribution);

        return report;
    }

}
