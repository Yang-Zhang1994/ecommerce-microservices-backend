package com.atguigu.gulimall.product.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

/**
 * sku销售属性&值
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-11-20 17:10:37
 */
@Data
@Entity
@Table(name = "pms_sku_sale_attr_value")
public class SkuSaleAttrValueEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "sku_id")
    private Long skuId;

    @Column(name = "attr_id")
    private Long attrId;

    /** 销售属性名 */
    @Column(name = "attr_name")
    private String attrName;

    /** 销售属性值 */
    @Column(name = "attr_value")
    private String attrValue;

    /** 顺序 */
    @Column(name = "attr_sort")
    private Integer attrSort;
}
