/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.sys.service.impl;

import io.renren.modules.sys.dao.SysRoleMenuRepository;
import io.renren.modules.sys.entity.SysRoleMenuEntity;
import io.renren.modules.sys.service.SysRoleMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 角色与菜单对应关系
 *
 * @author Mark sunlightcs@gmail.com
 */
@Service("sysRoleMenuService")
public class SysRoleMenuServiceImpl implements SysRoleMenuService {

    @Autowired
    private SysRoleMenuRepository sysRoleMenuRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdate(Long roleId, List<Long> menuIdList) {
        sysRoleMenuRepository.deleteByRoleIdIn(Arrays.asList(roleId));

        if (menuIdList == null || menuIdList.size() == 0) {
            return;
        }

        for (Long menuId : menuIdList) {
            SysRoleMenuEntity entity = new SysRoleMenuEntity();
            entity.setMenuId(menuId);
            entity.setRoleId(roleId);
            sysRoleMenuRepository.save(entity);
        }
    }

    @Override
    public List<Long> queryMenuIdList(Long roleId) {
        return sysRoleMenuRepository.findMenuIdsByRoleId(roleId);
    }

    @Override
    @Transactional
    public int deleteBatch(Long[] roleIds) {
        sysRoleMenuRepository.deleteByRoleIdIn(Arrays.asList(roleIds));
        return roleIds.length;
    }

    @Override
    @Transactional
    public void deleteByMenuId(Long menuId) {
        sysRoleMenuRepository.deleteByMenuId(menuId);
    }
}
