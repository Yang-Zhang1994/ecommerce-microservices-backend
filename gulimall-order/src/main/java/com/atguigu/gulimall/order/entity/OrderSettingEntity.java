package com.atguigu.gulimall.order.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 订单配置信息
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-01 22:54:19
 */
@Data
@Entity
@Table(name = "oms_order_setting")
public class OrderSettingEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 秒杀订单超时关闭时间(分) */
    @Column(name = "flash_order_overtime")
    private Integer flashOrderOvertime;

    /** 正常订单超时时间(分) */
    @Column(name = "normal_order_overtime")
    private Integer normalOrderOvertime;

    /** 发货后自动确认收货时间（天） */
    @Column(name = "confirm_overtime")
    private Integer confirmOvertime;

    /** 自动完成交易时间，不能申请退货（天） */
    @Column(name = "finish_overtime")
    private Integer finishOvertime;

    /** 订单完成后自动好评时间（天） */
    @Column(name = "comment_overtime")
    private Integer commentOvertime;

    /** 会员等级【0-不限会员等级，全部通用；其他-对应的其他会员等级】 */
    @Column(name = "member_level")
    private Integer memberLevel;
}
