package com.example.api.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 用户订单
 */
@Data
@Entity
@Table(name = "user_order")
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    // 订单号
    private String orderNo;

    // 用户ID
    private String userId;

    // 起点
    private String origin;

    // 目的地
    private String destination;

    // 货物类型
    private String type;

    // 数量
    private Integer quantity;

    // 提货时间
    private String pickupDate;

    // 备注
    private String remark;

    // 订单金额
    private Double amount;

    // 订单状态: 0=待支付, 1=已支付, 2=运输中, 3=已完成, -1=已取消
    private Integer status;

    // 支付方式
    private String paymentMethod;

    // 支付时间
    private String paidAt;

    // 创建时间
    private String createAt;

    // 更新时间
    private String updateAt;

}
