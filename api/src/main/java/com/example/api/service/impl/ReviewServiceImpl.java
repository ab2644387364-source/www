package com.example.api.service.impl;

import com.example.api.model.entity.Order;
import com.example.api.model.entity.Review;
import com.example.api.repository.OrderRepository;
import com.example.api.repository.ReviewRepository;
import com.example.api.service.ReviewService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Resource
    private ReviewRepository reviewRepository;

    @Resource
    private OrderRepository orderRepository;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public Review createReview(String userId, String orderId, Integer rating, String content) throws Exception {
        // 检查订单是否存在且属于该用户
        Order order = orderRepository.findByIdAndUserId(orderId, userId);
        if (order == null) {
            throw new Exception("订单不存在或无权限");
        }

        // 检查订单是否已完成 (status=3 表示已完成)
        if (order.getStatus() != 3) {
            throw new Exception("只能对已完成的订单进行评价");
        }

        // 检查是否已评价
        if (reviewRepository.existsByOrderIdAndUserId(orderId, userId)) {
            throw new Exception("该订单已评价，不能重复评价");
        }

        // 验证评分范围
        if (rating == null || rating < 1 || rating > 5) {
            throw new Exception("评分必须在1-5之间");
        }

        // 创建评价
        Review review = new Review();
        review.setOrderId(orderId);
        review.setUserId(userId);
        review.setRating(rating);
        review.setContent(content != null ? content.trim() : "");
        review.setCreateAt(dateFormat.format(new Date()));

        return reviewRepository.save(review);
    }

    @Override
    public Review getReviewByOrderId(String orderId) {
        return reviewRepository.findByOrderId(orderId).orElse(null);
    }

    @Override
    public boolean hasReviewed(String userId, String orderId) {
        return reviewRepository.existsByOrderIdAndUserId(orderId, userId);
    }

}
