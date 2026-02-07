package com.example.api.controller;

import com.example.api.model.document.GpsPoint;
import com.example.api.model.document.GpsTrack;
import com.example.api.model.support.ResponseResult;
import com.example.api.service.GpsTrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * GPS 轨迹控制器
 */
@RestController
@RequestMapping("/api/gps")
public class GpsTrackController {

    @Autowired
    private GpsTrackService gpsTrackService;

    /**
     * 创建轨迹记录
     */
    @PostMapping("/track/create")
    public ResponseResult<GpsTrack> createTrack(@RequestBody Map<String, String> body) {
        try {
            String distributionId = body.get("distributionId");
            String vehicleId = body.get("vehicleId");
            String vehiclePlate = body.get("vehiclePlate");
            String driverName = body.get("driverName");

            if (distributionId == null || distributionId.isEmpty()) {
                return new ResponseResult<>(400, "配送单ID不能为空");
            }

            GpsTrack track = gpsTrackService.getOrCreateTrack(distributionId, vehicleId, vehiclePlate, driverName);
            return new ResponseResult<>(track);
        } catch (Exception e) {
            return new ResponseResult<>(500, "创建轨迹失败: " + e.getMessage());
        }
    }

    /**
     * 上报GPS位置
     */
    @PostMapping("/track/report")
    public ResponseResult<GpsTrack> reportLocation(@RequestBody Map<String, Object> body) {
        try {
            String distributionId = (String) body.get("distributionId");
            Double longitude = toDouble(body.get("longitude"));
            Double latitude = toDouble(body.get("latitude"));
            Double speed = toDouble(body.get("speed"));
            String address = (String) body.get("address");

            if (distributionId == null || longitude == null || latitude == null) {
                return new ResponseResult<>(400, "参数不完整");
            }

            GpsTrack track = gpsTrackService.addPoint(distributionId, longitude, latitude, speed, address);
            if (track != null) {
                return new ResponseResult<>(track);
            }
            return new ResponseResult<>(404, "轨迹记录不存在，请先创建");
        } catch (Exception e) {
            return new ResponseResult<>(500, "上报位置失败: " + e.getMessage());
        }
    }

    /**
     * 获取配送单的完整轨迹
     */
    @GetMapping("/track/{distributionId}")
    public ResponseResult<GpsTrack> getTrack(@PathVariable String distributionId) {
        try {
            GpsTrack track = gpsTrackService.getTrackByDistributionId(distributionId);
            if (track != null) {
                return new ResponseResult<>(track);
            }
            return new ResponseResult<>(404, "轨迹记录不存在");
        } catch (Exception e) {
            return new ResponseResult<>(500, "获取轨迹失败: " + e.getMessage());
        }
    }

    /**
     * 获取最新位置
     */
    @GetMapping("/latest/{distributionId}")
    public ResponseResult<GpsPoint> getLatestLocation(@PathVariable String distributionId) {
        try {
            GpsPoint point = gpsTrackService.getLatestPoint(distributionId);
            if (point != null) {
                return new ResponseResult<>(point);
            }
            return new ResponseResult<>(404, "暂无位置信息");
        } catch (Exception e) {
            return new ResponseResult<>(500, "获取位置失败: " + e.getMessage());
        }
    }

    private Double toDouble(Object value) {
        if (value == null)
            return null;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
