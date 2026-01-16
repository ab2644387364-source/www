package com.example.api.service;

import com.example.api.model.entity.TrackRecord;
import com.example.api.repository.TrackRecordRepository;
import com.example.api.utils.DataTimeUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class TrackRecordService {

    @Resource
    private TrackRecordRepository trackRecordRepository;

    @Resource
    private NotificationService notificationService;

    /**
     * 添加追踪记录
     */
    public TrackRecord addRecord(TrackRecord record) {
        record.setCreateTime(DataTimeUtil.getNowTimeString());
        TrackRecord saved = trackRecordRepository.save(record);

        // 创建通知
        String title = "物流更新: " + record.getNode();
        String content = record.getStatus() + " - " + (record.getRemark() != null ? record.getRemark() : "");
        notificationService.createTransportNotification(title, content, record.getDistributionId());

        return saved;
    }

    /**
     * 获取配送单的追踪记录
     */
    public List<TrackRecord> findByDistributionId(String distributionId) {
        return trackRecordRepository.findByDistributionIdOrderByCreateTimeDesc(distributionId);
    }

    /**
     * 删除记录
     */
    public void delete(String id) {
        trackRecordRepository.deleteById(id);
    }
}
