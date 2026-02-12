package com.atguigu.gulimall.order.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 退货原因
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-01 22:54:19
 */
@Data
@Entity
@Table(name = "oms_order_return_reason")
public class OrderReturnReasonEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 退货原因名 */
    @Column(name = "name")
    private String name;

    /** 排序 */
    @Column(name = "sort")
    private Integer sort;

    /** 启用状态 */
    @Column(name = "status")
    private Integer status;

    @Column(name = "create_time")
    private Date createTime;
}
