package com.atguigu.gulimall.ware.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 库存工作单
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-02 13:48:24
 */
@Data
@Entity
@Table(name = "wms_ware_order_task")
public class WareOrderTaskEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "order_sn")
    private String orderSn;

    /** 收货人 */
    @Column(name = "consignee")
    private String consignee;

    /** 收货人电话 */
    @Column(name = "consignee_tel")
    private String consigneeTel;

    /** 配送地址 */
    @Column(name = "delivery_address")
    private String deliveryAddress;

    /** 订单备注 */
    @Column(name = "order_comment")
    private String orderComment;

    /** 付款方式【 1:在线付款 2:货到付款】 */
    @Column(name = "payment_way")
    private Integer paymentWay;

    /** 任务状态 */
    @Column(name = "task_status")
    private Integer taskStatus;

    /** 订单描述 */
    @Column(name = "order_body")
    private String orderBody;

    /** 物流单号 */
    @Column(name = "tracking_no")
    private String trackingNo;

    @Column(name = "create_time")
    private Date createTime;

    /** 仓库id */
    @Column(name = "ware_id")
    private Long wareId;

    /** 工作单备注 */
    @Column(name = "task_comment")
    private String taskComment;
}
