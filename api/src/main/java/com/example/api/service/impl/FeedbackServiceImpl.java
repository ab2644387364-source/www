package com.example.api.service.impl;

import com.example.api.model.entity.Feedback;
import com.example.api.repository.FeedbackRepository;
import com.example.api.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 用户反馈服务实现
 */
@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Feedback create(Feedback feedback) {
        String now = LocalDateTime.now().format(FORMATTER);
        feedback.setStatus("pending");
        feedback.setCreateTime(now);
        feedback.setUpdateTime(now);
        return feedbackRepository.save(feedback);
    }

    @Override
    public List<Feedback> getByUserId(String userId) {
        return feedbackRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    @Override
    public List<Feedback> getAll() {
        return feedbackRepository.findAllByOrderByCreateTimeDesc();
    }

    @Override
    public List<Feedback> getByStatus(String status) {
        return feedbackRepository.findByStatusOrderByCreateTimeDesc(status);
    }

    @Override
    public Feedback reply(String id, String reply) {
        Feedback feedback = feedbackRepository.findById(id).orElse(null);
        if (feedback != null) {
            String now = LocalDateTime.now().format(FORMATTER);
            feedback.setReply(reply);
            feedback.setReplyTime(now);
            feedback.setStatus("resolved");
            feedback.setUpdateTime(now);
            return feedbackRepository.save(feedback);
        }
        return null;
    }

    @Override
    public Feedback updateStatus(String id, String status) {
        Feedback feedback = feedbackRepository.findById(id).orElse(null);
        if (feedback != null) {
            String now = LocalDateTime.now().format(FORMATTER);
            feedback.setStatus(status);
            feedback.setUpdateTime(now);
            return feedbackRepository.save(feedback);
        }
        return null;
    }

    @Override
    public Feedback getById(String id) {
        return feedbackRepository.findById(id).orElse(null);
    }
}
