package com.atguigu.gulimall.ware.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 仓库信息
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-02 13:48:24
 */
@Data
@Entity
@Table(name = "wms_ware_info")
public class WareInfoEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 仓库名 */
    @Column(name = "name")
    private String name;

    /** 仓库地址 */
    @Column(name = "address")
    private String address;

    /** 区域编码 */
    @Column(name = "areacode")
    private String areacode;
}
