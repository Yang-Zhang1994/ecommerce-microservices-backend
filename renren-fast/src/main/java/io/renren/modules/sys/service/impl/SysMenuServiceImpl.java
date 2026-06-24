/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.sys.service.impl;

import io.renren.common.utils.Constant;
import io.renren.modules.sys.dao.SysMenuRepository;
import io.renren.modules.sys.entity.SysMenuEntity;
import io.renren.modules.sys.service.SysMenuService;
import io.renren.modules.sys.service.SysRoleMenuService;
import io.renren.modules.sys.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service("sysMenuService")
public class SysMenuServiceImpl implements SysMenuService {
    @Autowired
    private SysMenuRepository sysMenuRepository;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysRoleMenuService sysRoleMenuService;

    @Override
    public List<SysMenuEntity> queryListParentId(Long parentId, List<Long> menuIdList) {
        List<SysMenuEntity> menuList = queryListParentId(parentId);
        if (menuIdList == null) {
            return menuList;
        }
        List<SysMenuEntity> userMenuList = new ArrayList<>();
        for (SysMenuEntity menu : menuList) {
            if (menuIdList.contains(menu.getMenuId())) {
                userMenuList.add(menu);
            }
        }
        return userMenuList;
    }

    @Override
    public List<SysMenuEntity> list() {
        return sysMenuRepository.findAll();
    }

    @Override
    public SysMenuEntity getById(Long menuId) {
        return sysMenuRepository.findById(menuId).orElse(null);
    }

    @Override
    public boolean save(SysMenuEntity menu) {
        sysMenuRepository.save(menu);
        return true;
    }

    @Override
    public boolean updateById(SysMenuEntity menu) {
        sysMenuRepository.save(menu);
        return true;
    }

    @Override
    public List<SysMenuEntity> queryListParentId(Long parentId) {
        return sysMenuRepository.findByParentIdOrderByOrderNumAsc(parentId);
    }

    @Override
    public List<SysMenuEntity> queryNotButtonList() {
        return sysMenuRepository.findByTypeNotOrderByOrderNumAsc(2);
    }

    @Override
    public List<SysMenuEntity> getUserMenuList(Long userId) {
        if (userId == Constant.SUPER_ADMIN) {
            return getMenuList(null);
        }
        List<Long> menuIdList = sysUserService.queryAllMenuId(userId);
        return getMenuList(menuIdList);
    }

    private List<SysMenuEntity> getMenuList(List<Long> menuIdList) {
        List<SysMenuEntity> menus = (menuIdList != null)
                ? sysMenuRepository.findAll().stream()
                .filter(m -> menuIdList.contains(m.getMenuId()) && (m.getType() == 0 || m.getType() == 1))
                .sorted(Comparator.comparing(SysMenuEntity::getOrderNum))
                .collect(java.util.stream.Collectors.toList())
                : sysMenuRepository.findAll().stream()
                .filter(m -> m.getType() == 0 || m.getType() == 1)
                .sorted(Comparator.comparing(SysMenuEntity::getOrderNum))
                .collect(java.util.stream.Collectors.toList());

        Map<Long, SysMenuEntity> menuMap = new HashMap<>(12);
        for (SysMenuEntity s : menus) {
            menuMap.put(s.getMenuId(), s);
        }
        Iterator<SysMenuEntity> iterator = menus.iterator();
        while (iterator.hasNext()) {
            SysMenuEntity menu = iterator.next();
            SysMenuEntity parent = menuMap.get(menu.getParentId());
            if (parent != null) {
                parent.getList().add(menu);
                iterator.remove();
            }
        }
        return menus;
    }

    @Override
    @Transactional
    public void delete(Long menuId) {
        sysMenuRepository.deleteById(menuId);
        sysRoleMenuService.deleteByMenuId(menuId);
    }

    private List<SysMenuEntity> getAllMenuList(List<Long> menuIdList) {
        List<SysMenuEntity> menuList = queryListParentId(0L, menuIdList);
        getMenuTreeList(menuList, menuIdList);
        return menuList;
    }

    private List<SysMenuEntity> getMenuTreeList(List<SysMenuEntity> menuList, List<Long> menuIdList) {
        for (SysMenuEntity entity : menuList) {
            if (entity.getType() == Constant.MenuType.CATALOG.getValue()) {
                entity.setList(getMenuTreeList(queryListParentId(entity.getMenuId(), menuIdList), menuIdList));
            }
        }
        return menuList;
    }
}
