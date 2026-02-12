package com.atguigu.gulimall.product.entity;

import com.atguigu.common.valid.AddGroup;
import com.atguigu.common.valid.ListValues;
import com.atguigu.common.valid.UpdateGroup;
import com.atguigu.common.valid.UpdateStatusGroup;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.io.Serializable;

/**
 * 品牌
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-11-20 17:10:37
 */
@Data
@Entity
@Table(name = "pms_brand")
public class BrandEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brand_id")
    @NotNull(message = "Brand ID must not be null", groups = {UpdateGroup.class})
    @Null(message = "Brand ID must be null when creating a new brand", groups = {AddGroup.class})
    private Long brandId;

    /** 品牌名 */
    @Column(name = "name")
    @NotBlank(message = "Brand name must not be blank", groups = {AddGroup.class, UpdateGroup.class})
    private String name;

    /** 品牌logo地址 */
    @Column(name = "logo")
    @NotBlank(groups = {AddGroup.class})
    @URL(message = "Logo must be a valid URL", groups = {AddGroup.class, UpdateGroup.class})
    private String logo;

    /** 介绍 */
    @Column(name = "descript")
    private String descript;

    /** 显示状态[0-不显示；1-显示] */
    @Column(name = "show_status")
    @NotNull(groups = {AddGroup.class, UpdateStatusGroup.class})
    @ListValues(values = {0, 1}, groups = {AddGroup.class, UpdateStatusGroup.class})
    private Integer showStatus;

    /** 检索首字母 */
    @Column(name = "first_letter")
    @NotEmpty(groups = {AddGroup.class})
    @Pattern(regexp = "^[a-zA-Z]$", message = "First letter must be a single alphabet character", groups = {AddGroup.class, UpdateGroup.class})
    private String firstLetter;

    /** 排序 */
    @Column(name = "sort")
    @NotNull(groups = {AddGroup.class})
    @Min(value = 0, message = "Sort must be a non-negative integer", groups = {AddGroup.class, UpdateGroup.class})
    private Integer sort;
}
