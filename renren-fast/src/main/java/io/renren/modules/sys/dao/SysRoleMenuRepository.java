/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 * https://www.renren.io
 * 版权所有，侵权必究！
 */
package io.renren.modules.sys.dao;

import io.renren.modules.sys.entity.SysRoleMenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 角色与菜单对应关系 (JPA)
 */
public interface SysRoleMenuRepository extends JpaRepository<SysRoleMenuEntity, Long> {

    @Query("SELECT rm.menuId FROM SysRoleMenuEntity rm WHERE rm.roleId = :roleId")
    List<Long> findMenuIdsByRoleId(@Param("roleId") Long roleId);

    void deleteByRoleIdIn(List<Long> roleIds);

    void deleteByMenuId(Long menuId);
}
