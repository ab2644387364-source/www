package com.example.api.controller;

import com.example.api.model.entity.Notification;
import com.example.api.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Resource
    private NotificationService notificationService;

    /**
     * 获取所有通知
     */
    @GetMapping("")
    public List<Notification> findAll() {
        return notificationService.findAll();
    }

    /**
     * 获取未读数量
     */
    @GetMapping("/unread-count")
    public Map<String, Object> getUnreadCount() {
        Map<String, Object> result = new HashMap<>();
        result.put("count", notificationService.getUnreadCount());
        return result;
    }

    /**
     * 标记单条为已读
     */
    @PutMapping("/{id}/read")
    public Notification markAsRead(@PathVariable String id) {
        return notificationService.markAsRead(id);
    }

    /**
     * 标记所有为已读
     */
    @PutMapping("/read-all")
    public Map<String, Object> markAllAsRead() {
        notificationService.markAllAsRead();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }

    /**
     * 创建通知（测试用）
     */
    @PostMapping("")
    public Notification create(@RequestBody Notification notification) {
        return notificationService.create(
                notification.getTitle(),
                notification.getContent(),
                notification.getType(),
                notification.getUserId(),
                notification.getDistributionId());
    }

    /**
     * 获取当前用户的通知
     */
    @GetMapping("/user/{userId}")
    public List<Notification> findByUserId(@PathVariable String userId) {
        return notificationService.findByUserId(userId);
    }

    /**
     * 获取用户的未读数量
     */
    @GetMapping("/user/{userId}/unread-count")
    public Map<String, Object> getUserUnreadCount(@PathVariable String userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("count", notificationService.getUnreadCountByUserId(userId));
        return result;
    }

    /**
     * 标记用户所有通知为已读
     */
    @PutMapping("/user/{userId}/read-all")
    public Map<String, Object> markAllAsReadByUser(@PathVariable String userId) {
        notificationService.markAllAsReadByUserId(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }
}
