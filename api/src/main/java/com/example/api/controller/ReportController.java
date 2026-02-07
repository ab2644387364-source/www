package com.example.api.controller;

import com.example.api.model.entity.Distribution;
import com.example.api.model.entity.Inventory;
import com.example.api.model.entity.Order;
import com.example.api.repository.DistributionRepository;
import com.example.api.repository.InventoryRepository;
import com.example.api.repository.OrderRepository;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报表导出控制器
 * 提供订单、配送、库存报表数据
 */
@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Resource
    private OrderRepository orderRepository;

    @Resource
    private DistributionRepository distributionRepository;

    @Resource
    private InventoryRepository inventoryRepository;

    /**
     * 订单报表数据
     * GET /api/report/orders?startDate=xxx&endDate=xxx
     */
    @GetMapping("/orders")
    public List<Map<String, Object>> getOrderReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        List<Order> orders = orderRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Order order : orders) {
            // 日期筛选
            if (startDate != null && !startDate.isEmpty() && order.getCreateAt() != null) {
                if (order.getCreateAt().compareTo(startDate) < 0)
                    continue;
            }
            if (endDate != null && !endDate.isEmpty() && order.getCreateAt() != null) {
                if (order.getCreateAt().compareTo(endDate) > 0)
                    continue;
            }

            Map<String, Object> row = new HashMap<>();
            row.put("orderId", order.getId());
            row.put("orderNo", order.getOrderNo());
            row.put("fromContact", order.getFromContact());
            row.put("fromPhone", order.getFromPhone());
            row.put("origin", order.getOrigin());
            row.put("toContact", order.getToContact());
            row.put("toPhone", order.getToPhone());
            row.put("destination", order.getDestination());
            row.put("type", order.getType());
            row.put("quantity", order.getQuantity());
            row.put("amount", order.getAmount());
            row.put("status", getOrderStatusText(order.getStatus()));
            row.put("paymentMethod", order.getPaymentMethod());
            row.put("createAt", order.getCreateAt());
            row.put("remark", order.getRemark());
            result.add(row);
        }

        return result;
    }

    /**
     * 配送报表数据
     * GET /api/report/distributions?startDate=xxx&endDate=xxx
     */
    @GetMapping("/distributions")
    public List<Map<String, Object>> getDistributionReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        List<Distribution> distributions = distributionRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Distribution dist : distributions) {
            // 日期筛选（使用 time 字段）
            if (startDate != null && !startDate.isEmpty() && dist.getTime() != null) {
                if (dist.getTime().compareTo(startDate) < 0)
                    continue;
            }
            if (endDate != null && !endDate.isEmpty() && dist.getTime() != null) {
                if (dist.getTime().compareTo(endDate) > 0)
                    continue;
            }

            Map<String, Object> row = new HashMap<>();
            row.put("id", dist.getId());
            row.put("orderNo", dist.getOrderNo());
            row.put("driver", dist.getDriver());
            row.put("number", dist.getNumber());
            row.put("origin", dist.getOrigin());
            row.put("destination", dist.getDestination());
            row.put("currentNode", dist.getCurrentNode());
            row.put("status", getDistributionStatusText(dist.getStatus()));
            row.put("warningLevel", getWarningLevelText(dist.getWarningLevel()));
            row.put("phone", dist.getPhone());
            row.put("address", dist.getAddress());
            row.put("time", dist.getTime());
            result.add(row);
        }

        return result;
    }

    /**
     * 库存报表数据
     * GET /api/report/inventory?wid=xxx
     */
    @GetMapping("/inventory")
    public List<Map<String, Object>> getInventoryReport(
            @RequestParam(required = false) String wid) {

        List<Inventory> inventories;
        if (wid != null && !wid.isEmpty()) {
            inventories = inventoryRepository.findAllByWid(wid);
        } else {
            inventories = inventoryRepository.findAll();
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (Inventory inv : inventories) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", inv.getId());
            row.put("wid", inv.getWid());
            row.put("cid", inv.getCid());
            row.put("name", inv.getName());
            row.put("location", inv.getLocation());
            row.put("count", inv.getCount());
            row.put("minCount", inv.getMinCount());
            row.put("updateAt", inv.getUpdateAt());
            row.put("isLowStock",
                    inv.getMinCount() != null && inv.getCount() != null && inv.getCount() <= inv.getMinCount() ? "是"
                            : "否");
            result.add(row);
        }

        return result;
    }

    /**
     * 获取报表统计概览
     */
    @GetMapping("/summary")
    public Map<String, Object> getReportSummary() {
        Map<String, Object> summary = new HashMap<>();

        // 订单统计
        long totalOrders = orderRepository.count();
        summary.put("totalOrders", totalOrders);

        // 配送统计
        long totalDistributions = distributionRepository.count();
        summary.put("totalDistributions", totalDistributions);

        // 库存统计
        long totalInventory = inventoryRepository.count();
        summary.put("totalInventory", totalInventory);

        return summary;
    }

    private String getOrderStatusText(Integer status) {
        if (status == null)
            return "未知";
        switch (status) {
            case 0:
                return "待支付";
            case 1:
                return "待审核";
            case 2:
                return "已审核";
            case 3:
                return "配送中";
            case 4:
                return "已送达";
            case 5:
                return "已签收";
            case 6:
                return "已取消";
            default:
                return "未知";
        }
    }

    private String getDistributionStatusText(Integer status) {
        if (status == null)
            return "未知";
        switch (status) {
            case 0:
                return "待调度";
            case 1:
                return "运输中";
            case 2:
                return "已完成";
            default:
                return "未知";
        }
    }

    private String getWarningLevelText(Integer level) {
        if (level == null)
            return "正常";
        switch (level) {
            case 0:
                return "正常";
            case 1:
                return "一般";
            case 2:
                return "严重";
            default:
                return "正常";
        }
    }
}
