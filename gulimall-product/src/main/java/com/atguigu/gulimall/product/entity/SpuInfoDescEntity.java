package com.atguigu.gulimall.product.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

/**
 * spu信息介绍
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-11-20 17:10:37
 */
@Data
@Entity
@Table(name = "pms_spu_info_desc")
public class SpuInfoDescEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 商品id (manually assigned, not auto-generated) */
    @Id
    @Column(name = "spu_id")
    private Long spuId;

    /** 商品介绍 */
    @Column(name = "decript")
    private String decript;
}
