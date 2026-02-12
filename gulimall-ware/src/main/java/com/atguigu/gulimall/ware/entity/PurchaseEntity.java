package com.atguigu.gulimall.ware.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 采购信息
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-02 13:48:24
 */
@Data
@Entity
@Table(name = "wms_purchase")
public class PurchaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 采购人id */
    @Column(name = "assignee_id")
    private Long assigneeId;

    /** 采购人名 */
    @Column(name = "assignee_name")
    private String assigneeName;

    /** 联系方式 */
    @Column(name = "phone")
    private String phone;

    /** 优先级 */
    @Column(name = "priority")
    private Integer priority;

    /** 状态 */
    @Column(name = "status")
    private Integer status;

    /** 仓库id */
    @Column(name = "ware_id")
    private Long wareId;

    /** 总金额 */
    @Column(name = "amount")
    private BigDecimal amount;

    /** 创建日期 */
    @Column(name = "create_time")
    private Date createTime;

    /** 更新日期 */
    @Column(name = "update_time")
    private Date updateTime;
}
