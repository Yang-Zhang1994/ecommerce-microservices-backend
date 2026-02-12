package com.atguigu.gulimall.ware.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 库存工作单
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-02 13:48:24
 */
@Data
@Entity
@Table(name = "wms_ware_order_task_detail")
public class WareOrderTaskDetailEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "sku_id")
    private Long skuId;

    @Column(name = "sku_name")
    private String skuName;

    /** 购买个数 */
    @Column(name = "sku_num")
    private Integer skuNum;

    /** 工作单id */
    @Column(name = "task_id")
    private Long taskId;
}
