package com.example.api.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * 系统通知
 */
@Data
@Entity
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    // 接收用户ID（null表示系统通知，所有人可见）
    private String userId;

    // 标题
    private String title;

    // 内容
    private String content;

    // 类型: system/transport/warning
    private String type;

    // 是否已读
    private Boolean isRead;

    // 创建时间
    private String createTime;

    // 关联的配送单ID（可选）
    private String distributionId;
}
