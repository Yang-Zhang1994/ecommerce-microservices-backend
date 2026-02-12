package com.atguigu.gulimall.ware.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 商品库存
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-02 13:48:24
 */
@Data
@Entity
@Table(name = "wms_ware_sku")
public class WareSkuEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "sku_id")
    private Long skuId;

    /** 仓库id */
    @Column(name = "ware_id")
    private Long wareId;

    /** 库存数 */
    @Column(name = "stock")
    private Integer stock;

    @Column(name = "sku_name")
    private String skuName;

    /** 锁定库存 */
    @Column(name = "stock_locked")
    private Integer stockLocked;
}
