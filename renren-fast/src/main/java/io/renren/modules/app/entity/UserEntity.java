/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.app.entity;

import lombok.Data;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Entity
@Table(name = "tb_user")
public class UserEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    /** 用户名 */
    @Column(name = "username")
    private String username;

    /** 手机号 */
    @Column(name = "mobile")
    private String mobile;

    /** 密码 */
    @Column(name = "password")
    private String password;

    /** 创建时间 */
    @Column(name = "create_time")
    private Date createTime;
}
