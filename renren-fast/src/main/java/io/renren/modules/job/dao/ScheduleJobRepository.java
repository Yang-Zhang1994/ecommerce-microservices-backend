/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 * https://www.renren.io
 * 版权所有，侵权必究！
 */
package io.renren.modules.job.dao;

import io.renren.modules.job.entity.ScheduleJobEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 定时任务 (JPA)
 */
public interface ScheduleJobRepository extends JpaRepository<ScheduleJobEntity, Long>, JpaSpecificationExecutor<ScheduleJobEntity> {

    @Query("SELECT j FROM ScheduleJobEntity j WHERE (COALESCE(:beanName, '') = '' OR j.beanName LIKE CONCAT('%', :beanName, '%'))")
    Page<ScheduleJobEntity> findFiltered(@Param("beanName") String beanName, Pageable pageable);

    @Modifying
    @Query("UPDATE ScheduleJobEntity j SET j.status = :status WHERE j.jobId IN :jobIds")
    int updateBatch(@Param("jobIds") List<Long> jobIds, @Param("status") Integer status);
}
