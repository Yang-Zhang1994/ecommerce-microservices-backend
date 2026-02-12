package com.atguigu.gulimall.product.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * spu信息
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-11-20 17:10:37
 */
@Data
@Entity
@Table(name = "pms_spu_info")
public class SpuInfoEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 商品名称 */
    @Column(name = "spu_name")
    private String spuName;

    /** 商品描述 */
    @Column(name = "spu_description")
    private String spuDescription;

    /** 所属分类id */
    @Column(name = "catalog_id")
    private Long catalogId;

    /** 品牌id */
    @Column(name = "brand_id")
    private Long brandId;

    @Column(name = "weight")
    private BigDecimal weight;

    /** 上架状态[0 - 下架，1 - 上架] */
    @Column(name = "publish_status")
    private Integer publishStatus;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;
}
