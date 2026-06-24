/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.job.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 定时任务日志
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Entity
@Table(name = "schedule_job_log")
public class ScheduleJobLogEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 日志id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    /** 任务id */
    @Column(name = "job_id")
    private Long jobId;

    /** spring bean名称 */
    @Column(name = "bean_name")
    private String beanName;

    /** 参数 */
    @Column(name = "params")
    private String params;

    /** 任务状态    0：成功    1：失败 */
    @Column(name = "status")
    private Integer status;

    /** 失败信息 */
    @Column(name = "error")
    private String error;

    /** 耗时(单位：毫秒) */
    @Column(name = "times")
    private Integer times;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "create_time")
    private Date createTime;
}
