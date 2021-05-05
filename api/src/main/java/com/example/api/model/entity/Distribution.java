package com.example.api.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * 转运
 */
@Data
@Entity
@NoArgsConstructor
public class Distribution {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    //司机id
    private String did;

    //车辆id
    private String vid;

    //司机
    private String driver;

    //车牌号
    private String number;

    //客户电话
    private String phone;

    //客户地址
    private String address;

    //起点
    private String origin;

    //目的地
    private String destination;

    //路线规划
    private String routePlan;

    //路线节点
    private String routeNodes;

    //当前位置
    private String currentNode;

    //异常等级 0-无 1-一般 2-严重
    private Integer warningLevel;

    //异常说明
    private String warningNote;

    //加急处理
    private boolean urgent;

    private String care;

    private String time;

    private Integer status;

}
