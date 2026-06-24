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
import java.io.Serializable;

/**
 * 角色与菜单对应关系
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Entity
@Table(name = "sys_role_menu")
public class SysRoleMenuEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 角色ID */
    @Column(name = "role_id")
    private Long roleId;

    /** 菜单ID */
    @Column(name = "menu_id")
    private Long menuId;
}
