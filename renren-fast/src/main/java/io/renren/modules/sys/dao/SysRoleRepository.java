/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 * https://www.renren.io
 * 版权所有，侵权必究！
 */
package io.renren.modules.sys.dao;

import io.renren.modules.sys.entity.SysRoleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 角色管理 (JPA)
 */
public interface SysRoleRepository extends JpaRepository<SysRoleEntity, Long> {

    @Query("SELECT r FROM SysRoleEntity r WHERE (COALESCE(:roleName, '') = '' OR r.roleName LIKE CONCAT('%', :roleName, '%')) AND (:createUserId IS NULL OR r.createUserId = :createUserId)")
    Page<SysRoleEntity> findFiltered(@Param("roleName") String roleName, @Param("createUserId") Long createUserId, Pageable pageable);

    @Query("SELECT r.roleId FROM SysRoleEntity r WHERE r.createUserId = :createUserId")
    List<Long> findRoleIdsByCreateUserId(@Param("createUserId") Long createUserId);

    List<SysRoleEntity> findByCreateUserId(Long createUserId);
}
