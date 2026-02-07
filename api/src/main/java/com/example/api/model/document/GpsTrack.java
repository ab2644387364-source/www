package com.example.api.model.document;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * GPS 轨迹记录（MongoDB 文档）
 */
@Data
@NoArgsConstructor
@Document(collection = "gps_tracks")
public class GpsTrack {

    @Id
    private String id;

    // 关联的配送单ID
    @Indexed
    private String distributionId;

    // 车辆ID
    @Indexed
    private String vehicleId;

    // 车牌号
    private String vehiclePlate;

    // 司机/押运员名称
    private String driverName;

    // 轨迹点列表
    private List<GpsPoint> points = new ArrayList<>();

    // 最新位置
    private GpsPoint latestPoint;

    // 总里程（公里）
    private Double totalDistance;

    // 创建时间
    private Date createTime;

    // 更新时间
    private Date updateTime;

    /**
     * 添加轨迹点
     */
    public void addPoint(GpsPoint point) {
        if (this.points == null) {
            this.points = new ArrayList<>();
        }
        this.points.add(point);
        this.latestPoint = point;
        this.updateTime = new Date();
    }
}
