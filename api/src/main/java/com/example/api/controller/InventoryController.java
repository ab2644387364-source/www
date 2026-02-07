package com.example.api.controller;

import com.example.api.model.entity.Inventory;
import com.example.api.model.entity.InventoryRecord;
import com.example.api.model.vo.CommodityChartVo;
import com.example.api.service.InventoryRecordService;
import com.example.api.service.InventoryService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Resource
    private InventoryService inventoryService;

    @Resource
    private InventoryRecordService recordService;

    @GetMapping("")
    public List<Inventory> findAll() {
        return inventoryService.findAll();
    }

    @GetMapping("analyze")
    public List<CommodityChartVo> analyze(Integer type) {
        return recordService.analyzeCommodity(type);
    }

    // 指定仓库id
    // 查询某个仓库的库存情况
    @GetMapping("/warehouse/{id}")
    public List<Inventory> findByWarehouse(@PathVariable String id) {
        return inventoryService.findByWarehouseId(id);
    }

    // 指定商品id
    // 查询某个商品在所有仓库的库存
    @GetMapping("/commodity/{id}")
    public List<Inventory> findByCommodity(@PathVariable String id) {
        return inventoryService.findByCommodityId(id);
    }

    // 指定仓库id
    // 查询某个仓库库内商品的出库入库记录
    @GetMapping("/record/warehouse/{id}")
    public List<InventoryRecord> findRecordByWarehouse(@PathVariable String id) {
        return recordService.findAllByWarehouseId(id);
    }

    // 指定商品id
    // 查询某个商品在所有仓库出库入库记录
    @GetMapping("/record/commodity/{id}")
    public List<InventoryRecord> findRecordByCommodity(@PathVariable String id) {
        return recordService.findAllByCommodityId(id);
    }

    @PostMapping("/in")
    public InventoryRecord in(@RequestBody InventoryRecord record) throws Exception {
        return recordService.in(record);
    }

    @PostMapping("/out")
    public InventoryRecord out(@RequestBody InventoryRecord record) throws Exception {
        return recordService.out(record);
    }

    // ========== 新增接口 ==========

    /**
     * 分页查询库存
     * GET /api/inventory/page?wid=xxx&keyword=xxx&page=0&size=10
     */
    @GetMapping("/page")
    public Page<Inventory> findAllPage(
            @RequestParam(required = false) String wid,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return inventoryService.findAllPage(wid, keyword, page, size);
    }

    /**
     * 获取库存预警列表
     * GET /api/inventory/warning
     */
    @GetMapping("/warning")
    public List<Inventory> findLowStock(@RequestParam(required = false) String wid) {
        if (wid != null && !wid.isEmpty()) {
            return inventoryService.findLowStockByWid(wid);
        }
        return inventoryService.findLowStock();
    }

    /**
     * 设置库存预警阈值
     * PUT /api/inventory/{id}/min-count
     */
    @PutMapping("/{id}/min-count")
    public ResponseEntity<?> updateMinCount(
            @PathVariable String id,
            @RequestBody Map<String, Integer> body) {
        try {
            Integer minCount = body.get("minCount");
            Inventory inventory = inventoryService.updateMinCount(id, minCount);
            return ResponseEntity.ok(inventory);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", false, "message", e.getMessage()));
        }
    }

    /**
     * 库存盘点
     * POST /api/inventory/check
     */
    @PostMapping("/check")
    public ResponseEntity<?> stockCheck(@RequestBody Map<String, Object> body) {
        try {
            String id = (String) body.get("id");
            Integer actualCount = (Integer) body.get("actualCount");
            String remark = (String) body.get("remark");
            Inventory inventory = inventoryService.stockCheck(id, actualCount, remark);
            return ResponseEntity.ok(inventory);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", false, "message", e.getMessage()));
        }
    }

    /**
     * 获取库存统计
     * GET /api/inventory/stats
     */
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return inventoryService.getStats();
    }

    /**
     * 根据ID获取库存详情
     * GET /api/inventory/{id}
     */
    @GetMapping("/{id}")
    public Inventory findById(@PathVariable String id) {
        return inventoryService.findById(id);
    }

}
