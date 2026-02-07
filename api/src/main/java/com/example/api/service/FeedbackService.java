package com.example.api.service;

import com.example.api.model.entity.Feedback;

import java.util.List;

/**
 * 用户反馈服务接口
 */
public interface FeedbackService {

    /**
     * 创建反馈
     */
    Feedback create(Feedback feedback);

    /**
     * 获取用户的反馈列表
     */
    List<Feedback> getByUserId(String userId);

    /**
     * 获取所有反馈（管理员）
     */
    List<Feedback> getAll();

    /**
     * 按状态筛选反馈（管理员）
     */
    List<Feedback> getByStatus(String status);

    /**
     * 管理员回复反馈
     */
    Feedback reply(String id, String reply);

    /**
     * 更新反馈状态
     */
    Feedback updateStatus(String id, String status);

    /**
     * 根据ID获取反馈详情
     */
    Feedback getById(String id);
}
