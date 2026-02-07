package com.example.api.service.impl;

import com.example.api.service.FreightEstimateService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 运费估算服务实现
 */
@Service
public class FreightEstimateServiceImpl implements FreightEstimateService {

    // 货物单价配置（单位：元）
    private static final Map<String, Double> UNIT_PRICES = new HashMap<>();
    static {
        UNIT_PRICES.put("牛", 200.0);
        UNIT_PRICES.put("羊", 50.0);
        UNIT_PRICES.put("猪", 80.0);
    }

    // 默认单价
    private static final double DEFAULT_UNIT_PRICE = 100.0;

    // 跨城运输系数
    private static final double CROSS_CITY_FACTOR = 1.5;

    @Override
    public Map<String, Object> estimateFreight(String origin, String destination, String type, Integer quantity) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> breakdown = new HashMap<>();

        // 获取单价
        double unitPrice = UNIT_PRICES.getOrDefault(type, DEFAULT_UNIT_PRICE);
        breakdown.put("unitPrice", unitPrice);
        breakdown.put("unitLabel", "元/" + getUnitLabel(type));

        // 数量
        int qty = (quantity != null && quantity > 0) ? quantity : 1;
        breakdown.put("quantity", qty);

        // 基础运费
        double baseFreight = unitPrice * qty;
        breakdown.put("baseFreight", baseFreight);

        // 距离系数（简化逻辑：同城为1.0，不同城市为1.5）
        double distanceFactor = isSameCity(origin, destination) ? 1.0 : CROSS_CITY_FACTOR;
        breakdown.put("distanceFactor", distanceFactor);
        breakdown.put("isCrossCity", distanceFactor > 1.0);

        // 最终运费
        double totalAmount = baseFreight * distanceFactor;
        // 保留两位小数
        totalAmount = Math.round(totalAmount * 100.0) / 100.0;

        result.put("amount", totalAmount);
        result.put("breakdown", breakdown);
        result.put("origin", origin);
        result.put("destination", destination);
        result.put("type", type);
        result.put("quantity", qty);

        return result;
    }

    /**
     * 判断是否同城
     */
    private boolean isSameCity(String origin, String destination) {
        if (origin == null || destination == null) {
            return false;
        }
        // 简化逻辑：取地址前2个字符比较（如"北京市xxx" vs "北京yyy"）
        String originCity = origin.length() >= 2 ? origin.substring(0, 2) : origin;
        String destCity = destination.length() >= 2 ? destination.substring(0, 2) : destination;
        return originCity.equals(destCity);
    }

    /**
     * 获取计量单位标签
     */
    private String getUnitLabel(String type) {
        if ("牛".equals(type))
            return "头";
        if ("羊".equals(type))
            return "只";
        if ("猪".equals(type))
            return "头";
        return "头";
    }
}
