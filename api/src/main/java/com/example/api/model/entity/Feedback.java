package com.example.api.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * 用户反馈/投诉
 */
@Data
@Entity
@NoArgsConstructor
public class Feedback {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    // 用户ID
    private String userId;

    // 用户名（冗余存储便于显示）
    private String username;

    // 类型: complaint(投诉)/suggestion(建议)/inquiry(咨询)
    private String type;

    // 标题
    private String title;

    // 内容
    @Column(columnDefinition = "TEXT")
    private String content;

    // 状态: pending(待处理)/processing(处理中)/resolved(已解决)/closed(已关闭)
    private String status;

    // 管理员回复
    @Column(columnDefinition = "TEXT")
    private String reply;

    // 回复时间
    private String replyTime;

    // 创建时间
    private String createTime;

    // 更新时间
    private String updateTime;
}
