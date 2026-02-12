package com.atguigu.gulimall.product.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

/**
 * sku图片
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-11-20 17:10:37
 */
@Data
@Entity
@Table(name = "pms_sku_images")
public class SkuImagesEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "sku_id")
    private Long skuId;

    /** 图片地址 */
    @Column(name = "img_url")
    private String imgUrl;

    /** 排序 */
    @Column(name = "img_sort")
    private Integer imgSort;

    /** 默认图[0 - 不是默认图，1 - 是默认图] */
    @Column(name = "default_img")
    private Integer defaultImg;
}
