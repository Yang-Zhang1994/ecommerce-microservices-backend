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
import java.util.Date;
import java.util.List;

/**
 * 角色
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Entity
@Table(name = "sys_role")
public class SysRoleEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 角色ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long roleId;

    /** 角色名称 */
    @NotBlank(message = "角色名称不能为空")
    @Column(name = "role_name")
    private String roleName;

    /** 备注 */
    @Column(name = "remark")
    private String remark;

    /** 创建者ID */
    @Column(name = "create_user_id")
    private Long createUserId;

    /** 菜单ID列表 */
    @Transient
    private List<Long> menuIdList;

    /** 创建时间 */
    @Column(name = "create_time")
    private Date createTime;
}
