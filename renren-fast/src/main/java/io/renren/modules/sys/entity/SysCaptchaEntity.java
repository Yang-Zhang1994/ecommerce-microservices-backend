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
import java.util.Date;

/**
 * 系统验证码
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Entity
@Table(name = "sys_captcha")
public class SysCaptchaEntity {
    /** uuid (manually assigned, primary key) */
    @Id
    @Column(name = "uuid")
    private String uuid;

    /** 验证码 */
    @Column(name = "code")
    private String code;

    /** 过期时间 */
    @Column(name = "expire_time")
    private Date expireTime;
}
