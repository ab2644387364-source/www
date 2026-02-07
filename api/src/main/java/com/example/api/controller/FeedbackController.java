package com.example.api.controller;

import com.example.api.model.entity.Feedback;
import com.example.api.model.support.ResponseResult;
import com.example.api.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户反馈控制器
 */
@RestController
@RequestMapping("/api")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    /**
     * 用户提交反馈
     */
    @PostMapping("/user/feedback")
    public ResponseResult<Feedback> create(@RequestBody Feedback feedback) {
        try {
            Feedback created = feedbackService.create(feedback);
            return new ResponseResult<>(created);
        } catch (Exception e) {
            return new ResponseResult<>(500, "提交反馈失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的反馈列表
     */
    @GetMapping("/user/feedback")
    public ResponseResult<List<Feedback>> getUserFeedback(@RequestParam String userId) {
        try {
            List<Feedback> list = feedbackService.getByUserId(userId);
            return new ResponseResult<>(list);
        } catch (Exception e) {
            return new ResponseResult<>(500, "获取反馈列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取反馈详情
     */
    @GetMapping("/user/feedback/{id}")
    public ResponseResult<Feedback> getDetail(@PathVariable String id) {
        try {
            Feedback feedback = feedbackService.getById(id);
            if (feedback != null) {
                return new ResponseResult<>(feedback);
            }
            return new ResponseResult<>(404, "反馈不存在");
        } catch (Exception e) {
            return new ResponseResult<>(500, "获取反馈详情失败: " + e.getMessage());
        }
    }

    /**
     * 管理员获取所有反馈
     */
    @GetMapping("/admin/feedback")
    public ResponseResult<List<Feedback>> getAllFeedback(@RequestParam(required = false) String status) {
        try {
            List<Feedback> list;
            if (status != null && !status.isEmpty()) {
                list = feedbackService.getByStatus(status);
            } else {
                list = feedbackService.getAll();
            }
            return new ResponseResult<>(list);
        } catch (Exception e) {
            return new ResponseResult<>(500, "获取反馈列表失败: " + e.getMessage());
        }
    }

    /**
     * 管理员回复反馈
     */
    @PutMapping("/admin/feedback/{id}/reply")
    public ResponseResult<Feedback> reply(@PathVariable String id, @RequestBody Map<String, String> body) {
        try {
            String reply = body.get("reply");
            if (reply == null || reply.trim().isEmpty()) {
                return new ResponseResult<>(400, "回复内容不能为空");
            }
            Feedback feedback = feedbackService.reply(id, reply);
            if (feedback != null) {
                return new ResponseResult<>(feedback);
            }
            return new ResponseResult<>(404, "反馈不存在");
        } catch (Exception e) {
            return new ResponseResult<>(500, "回复失败: " + e.getMessage());
        }
    }

    /**
     * 管理员更新反馈状态
     */
    @PutMapping("/admin/feedback/{id}/status")
    public ResponseResult<Feedback> updateStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        try {
            String status = body.get("status");
            Feedback feedback = feedbackService.updateStatus(id, status);
            if (feedback != null) {
                return new ResponseResult<>(feedback);
            }
            return new ResponseResult<>(404, "反馈不存在");
        } catch (Exception e) {
            return new ResponseResult<>(500, "更新状态失败: " + e.getMessage());
        }
    }
}
