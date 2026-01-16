package com.example.api.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * 物流追踪记录
 */
@Data
@Entity
@NoArgsConstructor
public class TrackRecord {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    // 关联的配送单ID
    private String distributionId;

    // 节点名称
    private String node;

    // 状态: 到达/离开/异常/装货/卸货
    private String status;

    // 备注说明
    private String remark;

    // 记录时间
    private String createTime;

    // 操作人
    private String operator;
}
