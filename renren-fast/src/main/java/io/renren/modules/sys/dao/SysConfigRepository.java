/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 * https://www.renren.io
 * 版权所有，侵权必究！
 */
package io.renren.modules.sys.dao;

import io.renren.modules.sys.entity.SysConfigEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 系统配置信息 (JPA)
 */
public interface SysConfigRepository extends JpaRepository<SysConfigEntity, Long> {

    @Query("SELECT c FROM SysConfigEntity c WHERE (COALESCE(:paramKey, '') = '' OR c.paramKey LIKE CONCAT('%', :paramKey, '%'))")
    Page<SysConfigEntity> findFiltered(@Param("paramKey") String paramKey, Pageable pageable);

    SysConfigEntity findByParamKey(String paramKey);

    @Modifying
    @Query("UPDATE SysConfigEntity c SET c.paramValue = :paramValue WHERE c.paramKey = :paramKey")
    int updateValueByKey(@Param("paramKey") String paramKey, @Param("paramValue") String paramValue);
}
