package com.example.api.service;

import com.example.api.model.entity.Inventory;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface InventoryService {

    Inventory save(Inventory inventory);

    List<Inventory> findAll();

    List<Inventory> findByCommodityId(String cid);

    List<Inventory> findByWarehouseId(String wid);

    // ========== 新增方法 ==========

    /**
     * 分页查询库存
     */
    Page<Inventory> findAllPage(String wid, String keyword, int page, int size);

    /**
     * 查询库存预警列表
     */
    List<Inventory> findLowStock();

    /**
     * 按仓库查询库存预警
     */
    List<Inventory> findLowStockByWid(String wid);

    /**
     * 设置库存预警阈值
     */
    Inventory updateMinCount(String id, Integer minCount) throws Exception;

    /**
     * 库存盘点
     */
    Inventory stockCheck(String id, Integer actualCount, String remark) throws Exception;

    /**
     * 获取库存统计
     */
    Map<String, Object> getStats();

    /**
     * 根据ID查询库存
     */
    Inventory findById(String id);

}
