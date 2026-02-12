package com.atguigu.gulimall.order.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 订单操作历史记录
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-01 22:54:19
 */
@Data
@Entity
@Table(name = "oms_order_operate_history")
public class OrderOperateHistoryEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 订单id */
    @Column(name = "order_id")
    private Long orderId;

    /** 操作人[用户；系统；后台管理员] */
    @Column(name = "operate_man")
    private String operateMan;

    /** 操作时间 */
    @Column(name = "create_time")
    private Date createTime;

    /** 订单状态【0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单】 */
    @Column(name = "order_status")
    private Integer orderStatus;

    /** 备注 */
    @Column(name = "note")
    private String note;
}
