/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.sys.entity;

import lombok.Data;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 系统配置信息
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Entity
@Table(name = "sys_config")
public class SysConfigEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "参数名不能为空")
    @Column(name = "param_key")
    private String paramKey;

    @NotBlank(message = "参数值不能为空")
    @Column(name = "param_value")
    private String paramValue;

    @Column(name = "remark")
    private String remark;
}
