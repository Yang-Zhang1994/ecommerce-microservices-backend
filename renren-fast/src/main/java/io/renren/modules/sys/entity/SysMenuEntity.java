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
import java.util.ArrayList;
import java.util.List;

/**
 * 菜单管理
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Entity
@Table(name = "sys_menu")
public class SysMenuEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 菜单ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Long menuId;

    /** 父菜单ID，一级菜单为0 */
    @Column(name = "parent_id")
    private Long parentId;

    /** 父菜单名称 */
    @Transient
    private String parentName;

    /** 菜单名称 */
    @Column(name = "name")
    private String name;

    /** 菜单URL */
    @Column(name = "url")
    private String url;

    /** 授权(多个用逗号分隔，如：user:list,user:create) */
    @Column(name = "perms")
    private String perms;

    /** 类型     0：目录   1：菜单   2：按钮 */
    @Column(name = "type")
    private Integer type;

    /** 菜单图标 */
    @Column(name = "icon")
    private String icon;

    /** 排序 */
    @Column(name = "order_num")
    private Integer orderNum;

    /** ztree属性 */
    @Transient
    private Boolean open;

    @Transient
    private List<SysMenuEntity> list = new ArrayList<>();
}
