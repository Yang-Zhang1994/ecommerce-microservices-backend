package com.atguigu.gulimall.product.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

/**
 * spu图片
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-11-20 17:10:37
 */
@Data
@Entity
@Table(name = "pms_spu_images")
public class SpuImagesEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "spu_id")
    private Long spuId;

    /** 图片名 */
    @Column(name = "img_name")
    private String imgName;

    /** 图片地址 */
    @Column(name = "img_url")
    private String imgUrl;

    /** 顺序 */
    @Column(name = "img_sort")
    private Integer imgSort;

    /** 是否默认图 */
    @Column(name = "default_img")
    private Integer defaultImg;
}
