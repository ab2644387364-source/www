package com.example.api.service;

import com.example.api.model.document.GpsPoint;
import com.example.api.model.document.GpsTrack;
import com.example.api.repository.GpsTrackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

/**
 * GPS 轨迹服务
 */
@Service
public class GpsTrackService {

    @Autowired
    private GpsTrackRepository gpsTrackRepository;

    /**
     * 创建或获取轨迹记录
     */
    public GpsTrack getOrCreateTrack(String distributionId, String vehicleId, String vehiclePlate, String driverName) {
        Optional<GpsTrack> existing = gpsTrackRepository.findByDistributionId(distributionId);
        if (existing.isPresent()) {
            return existing.get();
        }

        GpsTrack track = new GpsTrack();
        track.setDistributionId(distributionId);
        track.setVehicleId(vehicleId);
        track.setVehiclePlate(vehiclePlate);
        track.setDriverName(driverName);
        track.setTotalDistance(0.0);
        track.setCreateTime(new Date());
        track.setUpdateTime(new Date());
        return gpsTrackRepository.save(track);
    }

    /**
     * 添加GPS轨迹点
     */
    public GpsTrack addPoint(String distributionId, Double longitude, Double latitude, Double speed, String address) {
        Optional<GpsTrack> trackOpt = gpsTrackRepository.findByDistributionId(distributionId);
        if (!trackOpt.isPresent()) {
            return null;
        }

        GpsTrack track = trackOpt.get();
        GpsPoint point = new GpsPoint(longitude, latitude, speed, new Date(), address);

        // 计算距离增量
        if (track.getLatestPoint() != null) {
            double distance = calculateDistance(
                    track.getLatestPoint().getLatitude(), track.getLatestPoint().getLongitude(),
                    latitude, longitude);
            track.setTotalDistance(track.getTotalDistance() + distance);
        }

        track.addPoint(point);
        return gpsTrackRepository.save(track);
    }

    /**
     * 获取配送单的完整轨迹
     */
    public GpsTrack getTrackByDistributionId(String distributionId) {
        return gpsTrackRepository.findByDistributionId(distributionId).orElse(null);
    }

    /**
     * 获取最新位置
     */
    public GpsPoint getLatestPoint(String distributionId) {
        GpsTrack track = gpsTrackRepository.findByDistributionId(distributionId).orElse(null);
        return track != null ? track.getLatestPoint() : null;
    }

    /**
     * 计算两点之间的距离（Haversine公式，返回公里）
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371; // 地球半径（公里）
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
