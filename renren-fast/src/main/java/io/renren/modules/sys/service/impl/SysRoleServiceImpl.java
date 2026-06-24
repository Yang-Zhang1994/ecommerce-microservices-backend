/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.sys.service.impl;

import io.renren.common.exception.RRException;
import io.renren.common.utils.Constant;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;
import io.renren.modules.sys.dao.SysRoleRepository;
import io.renren.modules.sys.dao.SysUserRepository;
import io.renren.modules.sys.entity.SysRoleEntity;
import io.renren.modules.sys.service.SysRoleMenuService;
import io.renren.modules.sys.service.SysRoleService;
import io.renren.modules.sys.service.SysUserRoleService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 角色
 *
 * @author Mark sunlightcs@gmail.com
 */
@Service("sysRoleService")
public class SysRoleServiceImpl implements SysRoleService {
    @Autowired
    private SysRoleRepository sysRoleRepository;
    @Autowired
    private SysRoleMenuService sysRoleMenuService;
    @Autowired
    private SysUserRepository sysUserRepository;
    @Autowired
    private SysUserRoleService sysUserRoleService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String roleName = (String) params.get("roleName");
        Object cu = params.get("createUserId");
        Long createUserId = cu != null ? (cu instanceof Number ? ((Number) cu).longValue() : Long.parseLong(cu.toString())) : null;
        Pageable pageable = Query.getPageable(params);
        Page<SysRoleEntity> page = sysRoleRepository.findFiltered(
                StringUtils.isNotBlank(roleName) ? roleName : null,
                createUserId,
                pageable);
        return new PageUtils(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRole(SysRoleEntity role) {
        role.setCreateTime(new Date());
        sysRoleRepository.save(role);

        checkPrems(role);
        sysRoleMenuService.saveOrUpdate(role.getRoleId(), role.getMenuIdList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysRoleEntity role) {
        sysRoleRepository.save(role);

        checkPrems(role);
        sysRoleMenuService.saveOrUpdate(role.getRoleId(), role.getMenuIdList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(Long[] roleIds) {
        sysRoleRepository.deleteAllById(Arrays.asList(roleIds));
        sysRoleMenuService.deleteBatch(roleIds);
        sysUserRoleService.deleteBatch(roleIds);
    }

    @Override
    public SysRoleEntity getById(Long roleId) {
        return sysRoleRepository.findById(roleId).orElse(null);
    }

    @Override
    public List<Long> queryRoleIdList(Long createUserId) {
        return sysRoleRepository.findRoleIdsByCreateUserId(createUserId);
    }

    @Override
    public List<SysRoleEntity> listByCreateUserId(Long createUserId) {
        if (createUserId == null) {
            return sysRoleRepository.findAll();
        }
        return sysRoleRepository.findByCreateUserId(createUserId);
    }

    private void checkPrems(SysRoleEntity role) {
        if (role.getCreateUserId() == Constant.SUPER_ADMIN) {
            return;
        }
        List<Long> menuIdList = sysUserRepository.queryAllMenuId(role.getCreateUserId());
        if (!menuIdList.containsAll(role.getMenuIdList())) {
            throw new RRException("新增角色的权限，已超出你的权限范围");
        }
    }
}
