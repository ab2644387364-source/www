package com.example.api.repository;

import com.example.api.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

        List<Order> findByUserIdOrderByCreateAtDesc(String userId);

        List<Order> findByUserIdAndStatusOrderByCreateAtDesc(String userId, Integer status);

        Order findByOrderNo(String orderNo);

        Order findByIdAndUserId(String id, String userId);

        // 分页查询用户订单
        Page<Order> findByUserIdOrderByCreateAtDesc(String userId, Pageable pageable);

        // 按状态+分页查询
        Page<Order> findByUserIdAndStatusOrderByCreateAtDesc(String userId, Integer status, Pageable pageable);

        // 关键词搜索（订单号、起点、终点）
        @Query("SELECT o FROM Order o WHERE o.userId = :userId AND " +
                        "(o.orderNo LIKE %:keyword% OR o.origin LIKE %:keyword% OR o.destination LIKE %:keyword%)")
        Page<Order> searchByKeyword(@Param("userId") String userId, @Param("keyword") String keyword,
                        Pageable pageable);

        // 关键词+状态搜索
        @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.status = :status AND " +
                        "(o.orderNo LIKE %:keyword% OR o.origin LIKE %:keyword% OR o.destination LIKE %:keyword%)")
        Page<Order> searchByKeywordAndStatus(@Param("userId") String userId, @Param("keyword") String keyword,
                        @Param("status") Integer status, Pageable pageable);

        // ========== 管理端查询方法 ==========

        // 管理端：获取所有订单（分页）
        Page<Order> findAllByOrderByCreateAtDesc(Pageable pageable);

        // 管理端：按状态筛选订单（分页）
        Page<Order> findByStatusOrderByCreateAtDesc(Integer status, Pageable pageable);

        // 管理端：关键词搜索（订单号、起点、终点、联系人）
        @Query("SELECT o FROM Order o WHERE " +
                        "(o.orderNo LIKE %:keyword% OR o.origin LIKE %:keyword% OR o.destination LIKE %:keyword% " +
                        "OR o.fromContact LIKE %:keyword% OR o.toContact LIKE %:keyword%) " +
                        "ORDER BY o.createAt DESC")
        Page<Order> searchByKeywordForAdmin(@Param("keyword") String keyword, Pageable pageable);

        // 管理端：关键词+状态搜索
        @Query("SELECT o FROM Order o WHERE o.status = :status AND " +
                        "(o.orderNo LIKE %:keyword% OR o.origin LIKE %:keyword% OR o.destination LIKE %:keyword% " +
                        "OR o.fromContact LIKE %:keyword% OR o.toContact LIKE %:keyword%) " +
                        "ORDER BY o.createAt DESC")
        Page<Order> searchByKeywordAndStatusForAdmin(@Param("keyword") String keyword,
                        @Param("status") Integer status, Pageable pageable);

        // 统计各状态订单数量
        long countByStatus(Integer status);

        // ========== 用户仪表盘统计 ==========

        // 统计用户各状态订单数量
        long countByUserIdAndStatus(String userId, Integer status);

        // 统计用户订单总数
        long countByUserId(String userId);

        // 统计用户累计消费金额
        @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o WHERE o.userId = :userId AND o.status >= 1")
        Double sumAmountByUserId(@Param("userId") String userId);

        // 获取用户运输中的订单
        List<Order> findByUserIdAndStatusOrderByCreateAtDesc(String userId, int status);

        // 获取用户已支付订单（status >= 指定值）
        List<Order> findByUserIdAndStatusGreaterThanEqual(String userId, int status);
}
