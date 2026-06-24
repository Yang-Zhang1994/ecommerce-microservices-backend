/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 * https://www.renren.io
 * 版权所有，侵权必究！
 */
package io.renren.modules.job.dao;

import io.renren.modules.job.entity.ScheduleJobLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 定时任务日志 (JPA)
 */
public interface ScheduleJobLogRepository extends JpaRepository<ScheduleJobLogEntity, Long> {

    @Query("SELECT l FROM ScheduleJobLogEntity l WHERE (:jobId IS NULL OR l.jobId = :jobId)")
    Page<ScheduleJobLogEntity> findFiltered(@Param("jobId") Long jobId, Pageable pageable);
}
