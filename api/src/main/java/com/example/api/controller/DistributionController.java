package com.example.api.controller;

import com.example.api.model.entity.Distribution;
import com.example.api.model.entity.TrackRecord;
import com.example.api.repository.DistributionRepository;
import com.example.api.repository.DriverRepository;
import com.example.api.repository.VehicleRepository;
import com.example.api.service.DistributionService;
import com.example.api.service.TrackRecordService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/distribution")
public class DistributionController {

    @Resource
    private DistributionService distributionService;

    @Resource
    private DistributionRepository distributionRepository;

    @Resource
    private DriverRepository driverRepository;

    @Resource
    private VehicleRepository vehicleRepository;

    @Resource
    private TrackRecordService trackRecordService;

    @PostMapping("")
    public Distribution save(@RequestBody Distribution distribution) throws Exception {
        return distributionService.save(distribution);
    }

    @GetMapping("")
    public List<Distribution> findAll() {
        return distributionService.findAll();
    }

    @GetMapping("can")
    public Map<String, Object> can() {
        Map<String, Object> map = new HashMap<>();
        map.put("drivers", driverRepository.findAll());
        map.put("vehicles", vehicleRepository.findAll());
        return map;
    }

    /**
     * 根据订单号查询路线信息
     */
    @GetMapping("/route")
    public Map<String, Object> queryRoute(@RequestParam String orderNo) {
        Map<String, Object> result = new HashMap<>();
        result.put("distribution", null);
        result.put("trackRecords", List.of());
        if (orderNo == null || orderNo.trim().isEmpty()) {
            return result;
        }
        Distribution distribution = distributionRepository.findByOrderNo(orderNo.trim());
        if (distribution == null) {
            return result;
        }
        List<TrackRecord> records = trackRecordService.findByDistributionId(distribution.getId());
        result.put("distribution", distribution);
        result.put("trackRecords", records);
        return result;
    }

    /**
     * 获取配送单的追踪记录
     */
    @GetMapping("/{id}/track")
    public List<TrackRecord> getTrackRecords(@PathVariable String id) {
        return trackRecordService.findByDistributionId(id);
    }

    /**
     * 添加追踪记录
     */
    @PostMapping("/{id}/track")
    public TrackRecord addTrackRecord(@PathVariable String id, @RequestBody TrackRecord record) {
        record.setDistributionId(id);
        return trackRecordService.addRecord(record);
    }
}
