package com.example.api.controller;

import com.example.api.service.StatisticsService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Resource
    private StatisticsService statisticsService;

    /**
     * 获取总览统计数据
     */
    @GetMapping("/overview")
    public Map<String, Object> getOverview() {
        return statisticsService.getOverview();
    }

    /**
     * 获取运输状态统计
     */
    @GetMapping("/transport")
    public Map<String, Object> getTransportStats() {
        return statisticsService.getTransportStats();
    }

    /**
     * 获取近7天趋势数据
     */
    @GetMapping("/trend")
    public List<Map<String, Object>> getTrend() {
        return statisticsService.getTrend();
    }

    /**
     * 获取最近配送动态
     */
    @GetMapping("/activities")
    public List<Map<String, Object>> getRecentActivities() {
        return statisticsService.getRecentActivities();
    }

    /**
     * 获取所有仪表盘数据（一次性获取）
     */
    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard() {
        Map<String, Object> result = new HashMap<>();
        result.put("overview", statisticsService.getOverview());
        result.put("transport", statisticsService.getTransportStats());
        result.put("trend", statisticsService.getTrend());
        result.put("activities", statisticsService.getRecentActivities());
        return result;
    }
}
