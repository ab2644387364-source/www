package com.example.api.controller;

import com.example.api.service.FreightEstimateService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 运费估算控制器
 */
@RestController
@RequestMapping("/api/user/freight")
public class FreightController {

    @Resource
    private FreightEstimateService freightEstimateService;

    /**
     * 估算运费
     */
    @PostMapping("/estimate")
    public Map<String, Object> estimate(@RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();

        String origin = (String) body.get("origin");
        String destination = (String) body.get("destination");
        String type = (String) body.get("type");
        Integer quantity = null;
        Object qtyObj = body.get("quantity");
        if (qtyObj instanceof Integer) {
            quantity = (Integer) qtyObj;
        } else if (qtyObj instanceof Number) {
            quantity = ((Number) qtyObj).intValue();
        }

        // 参数校验
        if (origin == null || origin.trim().isEmpty()) {
            response.put("status", false);
            response.put("message", "请输入起点");
            return response;
        }
        if (destination == null || destination.trim().isEmpty()) {
            response.put("status", false);
            response.put("message", "请输入终点");
            return response;
        }
        if (type == null || type.trim().isEmpty()) {
            response.put("status", false);
            response.put("message", "请选择货物类型");
            return response;
        }
        if (quantity == null || quantity <= 0) {
            response.put("status", false);
            response.put("message", "请输入有效数量");
            return response;
        }

        Map<String, Object> result = freightEstimateService.estimateFreight(
                origin.trim(), destination.trim(), type.trim(), quantity);

        response.put("status", true);
        response.put("data", result);
        return response;
    }

    /**
     * 获取价格配置信息（供前端展示）
     */
    @GetMapping("/prices")
    public Map<String, Object> getPrices() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> prices = new HashMap<>();

        prices.put("牛", new HashMap<String, Object>() {
            {
                put("price", 200.0);
                put("unit", "头");
            }
        });
        prices.put("羊", new HashMap<String, Object>() {
            {
                put("price", 50.0);
                put("unit", "只");
            }
        });
        prices.put("猪", new HashMap<String, Object>() {
            {
                put("price", 80.0);
                put("unit", "头");
            }
        });

        response.put("status", true);
        response.put("data", prices);
        return response;
    }
}
