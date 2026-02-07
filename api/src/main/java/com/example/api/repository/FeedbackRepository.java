package com.example.api.repository;

import com.example.api.model.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户反馈数据访问层
 */
@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, String> {

    /**
     * 根据用户ID查询反馈列表，按创建时间倒序
     */
    List<Feedback> findByUserIdOrderByCreateTimeDesc(String userId);

    /**
     * 根据状态查询反馈列表
     */
    List<Feedback> findByStatusOrderByCreateTimeDesc(String status);

    /**
     * 查询所有反馈，按创建时间倒序
     */
    List<Feedback> findAllByOrderByCreateTimeDesc();
}
