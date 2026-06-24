/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 * https://www.renren.io
 * 版权所有，侵权必究！
 */
package io.renren.modules.sys.dao;

import io.renren.modules.sys.entity.SysUserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 用户与角色对应关系 (JPA)
 */
public interface SysUserRoleRepository extends JpaRepository<SysUserRoleEntity, Long> {

    void deleteByUserId(Long userId);

    @Query("SELECT ur.roleId FROM SysUserRoleEntity ur WHERE ur.userId = :userId")
    List<Long> findRoleIdsByUserId(@Param("userId") Long userId);

    void deleteByRoleIdIn(List<Long> roleIds);
}
