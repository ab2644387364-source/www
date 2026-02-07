package com.example.api.model.document;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * GPS 轨迹点（嵌入文档）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GpsPoint {

    // 经度
    private Double longitude;

    // 纬度
    private Double latitude;

    // 速度 km/h
    private Double speed;

    // 时间戳
    private Date timestamp;

    // 地址描述（可选，通过逆地理编码获取）
    private String address;
}
