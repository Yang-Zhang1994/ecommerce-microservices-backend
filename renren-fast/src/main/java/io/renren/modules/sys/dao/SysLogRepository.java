/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 * https://www.renren.io
 * 版权所有，侵权必究！
 */
package io.renren.modules.sys.dao;

import io.renren.modules.sys.entity.SysLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 系统日志 (JPA)
 */
public interface SysLogRepository extends JpaRepository<SysLogEntity, Long> {

    @Query("SELECT l FROM SysLogEntity l WHERE (COALESCE(:key, '') = '' OR l.username LIKE CONCAT('%', :key, '%'))")
    Page<SysLogEntity> findFiltered(@Param("key") String key, Pageable pageable);
}
