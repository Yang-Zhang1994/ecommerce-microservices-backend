/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 * https://www.renren.io
 * 版权所有，侵权必究！
 */
package io.renren.modules.sys.dao;

import io.renren.modules.sys.entity.SysUserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 系统用户 (JPA)
 */
public interface SysUserRepository extends JpaRepository<SysUserEntity, Long>, JpaSpecificationExecutor<SysUserEntity> {

    @Query("SELECT u FROM SysUserEntity u WHERE (COALESCE(:username, '') = '' OR u.username LIKE CONCAT('%', :username, '%')) AND (:createUserId IS NULL OR u.createUserId = :createUserId)")
    Page<SysUserEntity> findFiltered(@Param("username") String username, @Param("createUserId") Long createUserId, Pageable pageable);

    SysUserEntity findByUsername(String username);

    @Query(value = "SELECT m.perms FROM sys_user_role ur " +
            "LEFT JOIN sys_role_menu rm ON ur.role_id = rm.role_id " +
            "LEFT JOIN sys_menu m ON rm.menu_id = m.menu_id " +
            "WHERE ur.user_id = :userId", nativeQuery = true)
    List<String> queryAllPerms(@Param("userId") Long userId);

    @Query(value = "SELECT DISTINCT rm.menu_id FROM sys_user_role ur " +
            "LEFT JOIN sys_role_menu rm ON ur.role_id = rm.role_id " +
            "WHERE ur.user_id = :userId", nativeQuery = true)
    List<Long> queryAllMenuId(@Param("userId") Long userId);
}
