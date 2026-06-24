/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.sys.service.impl;

import io.renren.modules.sys.dao.SysUserRoleRepository;
import io.renren.modules.sys.entity.SysUserRoleEntity;
import io.renren.modules.sys.service.SysUserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 用户与角色对应关系
 *
 * @author Mark sunlightcs@gmail.com
 */
@Service("sysUserRoleService")
public class SysUserRoleServiceImpl implements SysUserRoleService {

    @Autowired
    private SysUserRoleRepository sysUserRoleRepository;

    @Override
    @Transactional
    public void saveOrUpdate(Long userId, List<Long> roleIdList) {
        sysUserRoleRepository.deleteByUserId(userId);

        if (roleIdList == null || roleIdList.size() == 0) {
            return;
        }

        for (Long roleId : roleIdList) {
            SysUserRoleEntity entity = new SysUserRoleEntity();
            entity.setUserId(userId);
            entity.setRoleId(roleId);
            sysUserRoleRepository.save(entity);
        }
    }

    @Override
    public List<Long> queryRoleIdList(Long userId) {
        return sysUserRoleRepository.findRoleIdsByUserId(userId);
    }

    @Override
    @Transactional
    public int deleteBatch(Long[] roleIds) {
        sysUserRoleRepository.deleteByRoleIdIn(Arrays.asList(roleIds));
        return roleIds.length;
    }
}
