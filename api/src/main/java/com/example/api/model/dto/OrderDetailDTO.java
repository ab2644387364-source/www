package com.example.api.model.dto;

import com.example.api.model.entity.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 订单详情聚合DTO
 * 包含订单基本信息、配送单、车辆、司机、轨迹记录
 */
@Data
@NoArgsConstructor
public class OrderDetailDTO {

    // 订单基本信息
    private Order order;

    // 配送单（若已派车）
    private Distribution distribution;

    // 车辆信息（若已派车）
    private Vehicle vehicle;

    // 司机信息（若已派车）
    private Driver driver;

    // 轨迹记录列表
    private List<TrackRecord> trackRecords;

    public OrderDetailDTO(Order order) {
        this.order = order;
    }
}
