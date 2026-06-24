/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.sys.entity;

import io.renren.common.validator.group.AddGroup;
import io.renren.common.validator.group.UpdateGroup;
import lombok.Data;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 系统用户
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Entity
@Table(name = "sys_user")
public class SysUserEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    /** 用户名 */
    @NotBlank(message = "用户名不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Column(name = "username")
    private String username;

    /** 密码 */
    @NotBlank(message = "密码不能为空", groups = AddGroup.class)
    @Column(name = "password")
    private String password;

    /** 盐 */
    @Column(name = "salt")
    private String salt;

    /** 邮箱 */
    @NotBlank(message = "邮箱不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Email(message = "邮箱格式不正确", groups = {AddGroup.class, UpdateGroup.class})
    @Column(name = "email")
    private String email;

    /** 手机号 */
    @Column(name = "mobile")
    private String mobile;

    /** 状态  0：禁用   1：正常 */
    @Column(name = "status")
    private Integer status;

    /** 角色ID列表 */
    @Transient
    private List<Long> roleIdList;

    /** 创建者ID */
    @Column(name = "create_user_id")
    private Long createUserId;

    /** 创建时间 */
    @Column(name = "create_time")
    private Date createTime;
}
