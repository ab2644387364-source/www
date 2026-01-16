package com.example.api.service;

import com.example.api.repository.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class StatisticsService {

    @Resource
    private CommodityRepository commodityRepository;

    @Resource
    private DistributionRepository distributionRepository;

    @Resource
    private WarehouseRepository warehouseRepository;

    @Resource
    private DriverRepository driverRepository;

    @Resource
    private VehicleRepository vehicleRepository;

    @Resource
    private SaleRepository saleRepository;

    /**
     * 获取总览统计数据
     */
    public Map<String, Object> getOverview() {
        Map<String, Object> result = new HashMap<>();

        // 牲畜种类数量
        result.put("commodityCount", commodityRepository.count());

        // 配送单总数
        result.put("distributionCount", distributionRepository.count());

        // 运输中订单
        result.put("transportingCount", distributionRepository.countByStatus(1));

        // 仓库数量
        result.put("warehouseCount", warehouseRepository.count());

        // 司机数量
        result.put("driverCount", driverRepository.count());

        // 车辆数量
        result.put("vehicleCount", vehicleRepository.count());

        // 销售订单数
        result.put("saleCount", saleRepository.count());

        return result;
    }

    /**
     * 获取运输状态统计
     */
    public Map<String, Object> getTransportStats() {
        Map<String, Object> result = new HashMap<>();

        // 待调度
        result.put("pending", distributionRepository.countByStatus(0));

        // 运输中
        result.put("transporting", distributionRepository.countByStatus(1));

        // 已完成
        result.put("completed", distributionRepository.countByStatus(2));

        // 异常订单（warningLevel > 0）
        result.put("warning", distributionRepository.countByWarningLevelGreaterThan(0));

        return result;
    }

    /**
     * 获取近7天运输趋势（模拟数据，实际应根据时间字段查询）
     */
    public List<Map<String, Object>> getTrend() {
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        // 获取近7天的日期
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.format(formatter));

            // 模拟数据 - 实际项目应该根据创建时间查询
            Random random = new Random(date.getDayOfMonth());
            dayData.put("created", random.nextInt(5) + 1);
            dayData.put("completed", random.nextInt(4) + 1);

            result.add(dayData);
        }

        return result;
    }

    /**
     * 获取最近的配送动态
     */
    public List<Map<String, Object>> getRecentActivities() {
        List<Map<String, Object>> result = new ArrayList<>();

        // 获取最近5条配送记录
        distributionRepository.findAll().stream()
                .limit(5)
                .forEach(dist -> {
                    Map<String, Object> activity = new HashMap<>();
                    activity.put("id", dist.getId());
                    activity.put("driver", dist.getDriver());
                    activity.put("number", dist.getNumber());
                    activity.put("origin", dist.getOrigin());
                    activity.put("destination", dist.getDestination());
                    activity.put("status", dist.getStatus());
                    activity.put("currentNode", dist.getCurrentNode());
                    activity.put("warningLevel", dist.getWarningLevel());
                    result.add(activity);
                });

        return result;
    }
}
