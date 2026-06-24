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
import java.util.Date;

/**
 * 系统用户Token
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Entity
@Table(name = "sys_user_token")
public class SysUserTokenEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 用户ID (manually assigned) */
    @Id
    @Column(name = "user_id")
    private Long userId;

    /** token */
    @Column(name = "token")
    private String token;

    /** 过期时间 */
    @Column(name = "expire_time")
    private Date expireTime;

    /** 更新时间 */
    @Column(name = "update_time")
    private Date updateTime;
}
