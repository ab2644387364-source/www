package com.example.api.service;

import java.util.Map;

/**
 * 运费估算服务
 */
public interface FreightEstimateService {

    /**
     * 估算运费
     * 
     * @param origin      起点
     * @param destination 终点
     * @param type        货物类型（牛、羊、猪）
     * @param quantity    数量
     * @return 包含运费金额和费用明细的Map
     */
    Map<String, Object> estimateFreight(String origin, String destination, String type, Integer quantity);
}
