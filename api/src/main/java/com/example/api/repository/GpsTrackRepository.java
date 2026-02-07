package com.example.api.repository;

import com.example.api.model.document.GpsTrack;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * GPS 轨迹 MongoDB 数据访问层
 */
@Repository
public interface GpsTrackRepository extends MongoRepository<GpsTrack, String> {

    /**
     * 根据配送单ID查询轨迹
     */
    Optional<GpsTrack> findByDistributionId(String distributionId);

    /**
     * 根据车辆ID查询轨迹
     */
    Optional<GpsTrack> findByVehicleId(String vehicleId);

    /**
     * 检查配送单是否已有轨迹记录
     */
    boolean existsByDistributionId(String distributionId);
}
