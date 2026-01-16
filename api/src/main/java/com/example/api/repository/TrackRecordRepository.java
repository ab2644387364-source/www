package com.example.api.repository;

import com.example.api.model.entity.TrackRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackRecordRepository extends JpaRepository<TrackRecord, String> {

    List<TrackRecord> findByDistributionIdOrderByCreateTimeDesc(String distributionId);
}
