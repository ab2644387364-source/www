package com.example.api.controller;

import com.example.api.model.entity.Review;
import com.example.api.service.ReviewService;
import com.example.api.utils.JwtTokenUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    @Resource
    private ReviewService reviewService;

    /**
     * 提交订单评价
     * POST /api/review
     */
    @PostMapping("")
    public Review createReview(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> body) throws Exception {
        String userId = getUserIdFromToken(token);
        String orderId = (String) body.get("orderId");
        Integer rating = (Integer) body.get("rating");
        String content = (String) body.get("content");
        return reviewService.createReview(userId, orderId, rating, content);
    }

    /**
     * 获取订单评价
     * GET /api/review/order/{orderId}
     */
    @GetMapping("/order/{orderId}")
    public Map<String, Object> getReviewByOrder(@PathVariable String orderId) {
        Map<String, Object> result = new HashMap<>();
        Review review = reviewService.getReviewByOrderId(orderId);
        result.put("hasReview", review != null);
        result.put("review", review);
        return result;
    }

    /**
     * 检查是否已评价
     * GET /api/review/check/{orderId}
     */
    @GetMapping("/check/{orderId}")
    public Map<String, Object> checkReviewed(
            @RequestHeader("Authorization") String token,
            @PathVariable String orderId) throws Exception {
        String userId = getUserIdFromToken(token);
        Map<String, Object> result = new HashMap<>();
        result.put("hasReviewed", reviewService.hasReviewed(userId, orderId));
        return result;
    }

    private String getUserIdFromToken(String token) throws Exception {
        String email = JwtTokenUtil.getUsername(token.replace("Bearer ", ""));
        return email;
    }

}
