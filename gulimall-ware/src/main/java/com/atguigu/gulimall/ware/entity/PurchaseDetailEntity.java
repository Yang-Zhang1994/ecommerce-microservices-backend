package com.atguigu.gulimall.ware.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-02 13:48:24
 */
@Data
@Entity
@Table(name = "wms_purchase_detail")
public class PurchaseDetailEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 采购单id */
    @Column(name = "purchase_id")
    private Long purchaseId;

    /** 采购商品id */
    @Column(name = "sku_id")
    private Long skuId;

    /** 采购数量 */
    @Column(name = "sku_num")
    private Integer skuNum;

    /** 采购金额 */
    @Column(name = "sku_price")
    private BigDecimal skuPrice;

    /** 仓库id */
    @Column(name = "ware_id")
    private Long wareId;

    /** 状态[0新建，1已分配，2正在采购，3已完成，4采购失败] */
    @Column(name = "status")
    private Integer status;
}
