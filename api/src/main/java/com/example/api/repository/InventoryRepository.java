package com.example.api.repository;

import com.example.api.model.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, String> {

    Inventory findByWidAndCid(String wid, String cid);

    List<Inventory> findAllByCid(String cid);

    List<Inventory> findAllByWid(String wid);

    // 分页查询所有库存
    Page<Inventory> findAllByOrderByUpdateAtDesc(Pageable pageable);

    // 按仓库分页查询
    Page<Inventory> findByWidOrderByUpdateAtDesc(String wid, Pageable pageable);

    // 库存预警查询（当前库存低于预警阈值）
    @Query("SELECT i FROM Inventory i WHERE i.minCount IS NOT NULL AND i.count <= i.minCount")
    List<Inventory> findLowStock();

    // 按仓库查询预警库存
    @Query("SELECT i FROM Inventory i WHERE i.wid = :wid AND i.minCount IS NOT NULL AND i.count <= i.minCount")
    List<Inventory> findLowStockByWid(@Param("wid") String wid);

    // 关键词搜索（商品名称）
    Page<Inventory> findByNameContainingOrderByUpdateAtDesc(String keyword, Pageable pageable);

    // 统计预警库存数量
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.minCount IS NOT NULL AND i.count <= i.minCount")
    long countLowStock();

}
