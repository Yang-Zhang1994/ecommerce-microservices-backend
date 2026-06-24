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
 * 系统日志
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Entity
@Table(name = "sys_log")
public class SysLogEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 用户名 */
    @Column(name = "username")
    private String username;

    /** 用户操作 */
    @Column(name = "operation")
    private String operation;

    /** 请求方法 */
    @Column(name = "method")
    private String method;

    /** 请求参数 */
    @Column(name = "params")
    private String params;

    /** 执行时长(毫秒) */
    @Column(name = "time")
    private Long time;

    /** IP地址 */
    @Column(name = "ip")
    private String ip;

    /** 创建时间 */
    @Column(name = "create_date")
    private Date createDate;
}
