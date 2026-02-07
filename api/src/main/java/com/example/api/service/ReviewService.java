package com.example.api.service;

import com.example.api.model.entity.Review;

public interface ReviewService {

    /**
     * 创建订单评价
     * 
     * @param userId  用户ID
     * @param orderId 订单ID
     * @param rating  评分 (1-5)
     * @param content 评价内容
     * @return 创建的评价
     */
    Review createReview(String userId, String orderId, Integer rating, String content) throws Exception;

    /**
     * 获取订单评价
     * 
     * @param orderId 订单ID
     * @return 评价信息，未评价返回null
     */
    Review getReviewByOrderId(String orderId);

    /**
     * 检查用户是否已对订单评价
     */
    boolean hasReviewed(String userId, String orderId);

}
