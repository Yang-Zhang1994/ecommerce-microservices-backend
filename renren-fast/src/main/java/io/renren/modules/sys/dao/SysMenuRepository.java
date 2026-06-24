/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 * https://www.renren.io
 * 版权所有，侵权必究！
 */
package io.renren.modules.sys.dao;

import io.renren.modules.sys.entity.SysMenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 菜单管理 (JPA)
 */
public interface SysMenuRepository extends JpaRepository<SysMenuEntity, Long> {

    List<SysMenuEntity> findByParentIdOrderByOrderNumAsc(Long parentId);

    List<SysMenuEntity> findByTypeNotOrderByOrderNumAsc(Integer type);
}
