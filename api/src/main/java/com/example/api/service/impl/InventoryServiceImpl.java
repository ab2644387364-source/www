package com.example.api.service.impl;

import com.example.api.model.entity.Inventory;
import com.example.api.model.entity.InventoryRecord;
import com.example.api.repository.InventoryRepository;
import com.example.api.service.InventoryRecordService;
import com.example.api.service.InventoryService;
import com.example.api.utils.DataTimeUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryServiceImpl implements InventoryService {

    @Resource
    private InventoryRepository inventoryRepository;

    @Resource
    private InventoryRecordService inventoryRecordService;

    @Override
    public Inventory save(Inventory inventory) {
        inventory.setUpdateAt(DataTimeUtil.getNowTimeString());
        return inventoryRepository.save(inventory);
    }

    @Override
    public List<Inventory> findAll() {
        return inventoryRepository.findAll();
    }

    @Override
    public List<Inventory> findByCommodityId(String cid) {
        return inventoryRepository.findAllByCid(cid);
    }

    @Override
    public List<Inventory> findByWarehouseId(String wid) {
        return inventoryRepository.findAllByWid(wid);
    }

    // ========== 新增方法实现 ==========

    @Override
    public Page<Inventory> findAllPage(String wid, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        boolean hasWid = wid != null && !wid.trim().isEmpty();
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();

        if (hasWid) {
            return inventoryRepository.findByWidOrderByUpdateAtDesc(wid, pageable);
        } else if (hasKeyword) {
            return inventoryRepository.findByNameContainingOrderByUpdateAtDesc(keyword.trim(), pageable);
        } else {
            return inventoryRepository.findAllByOrderByUpdateAtDesc(pageable);
        }
    }

    @Override
    public List<Inventory> findLowStock() {
        return inventoryRepository.findLowStock();
    }

    @Override
    public List<Inventory> findLowStockByWid(String wid) {
        return inventoryRepository.findLowStockByWid(wid);
    }

    @Override
    public Inventory updateMinCount(String id, Integer minCount) throws Exception {
        Inventory inventory = inventoryRepository.findById(id).orElse(null);
        if (inventory == null) {
            throw new Exception("库存记录不存在");
        }
        inventory.setMinCount(minCount);
        inventory.setUpdateAt(DataTimeUtil.getNowTimeString());
        return inventoryRepository.save(inventory);
    }

    @Override
    public Inventory stockCheck(String id, Integer actualCount, String remark) throws Exception {
        Inventory inventory = inventoryRepository.findById(id).orElse(null);
        if (inventory == null) {
            throw new Exception("库存记录不存在");
        }

        int difference = actualCount - (inventory.getCount() != null ? inventory.getCount() : 0);

        // 如果有差异，记录盘点调整
        if (difference != 0) {
            InventoryRecord record = new InventoryRecord();
            record.setName(inventory.getName());
            record.setWid(inventory.getWid());
            record.setCid(inventory.getCid());
            record.setCount(Math.abs(difference));
            record.setType(difference > 0 ? 1 : -1); // 正差异为入库，负差异为出库
            record.setDescription("盘点调整: " + (remark != null ? remark : ""));
            record.setCreateAt(DataTimeUtil.getNowTimeString());
            inventoryRecordService.save(record);
        }

        inventory.setCount(actualCount);
        inventory.setUpdateAt(DataTimeUtil.getNowTimeString());
        return inventoryRepository.save(inventory);
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", inventoryRepository.count()); // 总库存记录数
        stats.put("lowStock", inventoryRepository.countLowStock()); // 预警库存数

        // 计算总库存数量
        List<Inventory> all = inventoryRepository.findAll();
        int totalCount = all.stream()
                .mapToInt(i -> i.getCount() != null ? i.getCount() : 0)
                .sum();
        stats.put("totalCount", totalCount);

        return stats;
    }

    @Override
    public Inventory findById(String id) {
        return inventoryRepository.findById(id).orElse(null);
    }

}
