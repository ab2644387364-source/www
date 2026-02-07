package com.example.api.repository;

import com.example.api.model.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    List<Notification> findByUserIdOrUserIdIsNullOrderByCreateTimeDesc(String userId);

    long countByUserIdAndIsReadFalse(String userId);

    long countByUserIdIsNullAndIsReadFalse();

    long countByIsReadFalse();

    List<Notification> findAllByOrderByCreateTimeDesc();
}
