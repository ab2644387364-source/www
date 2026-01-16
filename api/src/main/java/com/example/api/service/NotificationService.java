package com.example.api.service;

import com.example.api.model.entity.Notification;
import com.example.api.repository.NotificationRepository;
import com.example.api.utils.DataTimeUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class NotificationService {

    @Resource
    private NotificationRepository notificationRepository;

    /**
     * 创建新通知
     */
    public Notification create(String title, String content, String type, String userId, String distributionId) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setUserId(userId);
        notification.setDistributionId(distributionId);
        notification.setIsRead(false);
        notification.setCreateTime(DataTimeUtil.getNowTimeString());
        return notificationRepository.save(notification);
    }

    /**
     * 创建系统通知（所有人可见）
     */
    public Notification createSystemNotification(String title, String content) {
        return create(title, content, "system", null, null);
    }

    /**
     * 创建运输通知
     */
    public Notification createTransportNotification(String title, String content, String distributionId) {
        return create(title, content, "transport", null, distributionId);
    }

    /**
     * 创建预警通知
     */
    public Notification createWarningNotification(String title, String content, String distributionId) {
        return create(title, content, "warning", null, distributionId);
    }

    /**
     * 获取所有通知
     */
    public List<Notification> findAll() {
        return notificationRepository.findAllByOrderByCreateTimeDesc();
    }

    /**
     * 获取用户的通知
     */
    public List<Notification> findByUserId(String userId) {
        return notificationRepository.findByUserIdOrUserIdIsNullOrderByCreateTimeDesc(userId);
    }

    /**
     * 获取未读数量
     */
    public long getUnreadCount() {
        return notificationRepository.countByIsReadFalse();
    }

    /**
     * 标记为已读
     */
    public Notification markAsRead(String id) {
        Notification notification = notificationRepository.findById(id).orElse(null);
        if (notification != null) {
            notification.setIsRead(true);
            return notificationRepository.save(notification);
        }
        return null;
    }

    /**
     * 标记所有为已读
     */
    public void markAllAsRead() {
        List<Notification> notifications = notificationRepository.findAll();
        notifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }
}
