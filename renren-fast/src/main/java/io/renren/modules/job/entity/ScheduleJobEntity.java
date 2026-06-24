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
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;

/**
 * 定时任务
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Entity
@Table(name = "schedule_job")
public class ScheduleJobEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 任务调度参数key */
    public static final String JOB_PARAM_KEY = "JOB_PARAM_KEY";

    /** 任务id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    private Long jobId;

    /** spring bean名称 */
    @NotBlank(message = "bean名称不能为空")
    @Column(name = "bean_name")
    private String beanName;

    /** 参数 */
    @Column(name = "params")
    private String params;

    /** cron表达式 */
    @NotBlank(message = "cron表达式不能为空")
    @Column(name = "cron_expression")
    private String cronExpression;

    /** 任务状态 */
    @Column(name = "status")
    private Integer status;

    /** 备注 */
    @Column(name = "remark")
    private String remark;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "create_time")
    private Date createTime;
}
