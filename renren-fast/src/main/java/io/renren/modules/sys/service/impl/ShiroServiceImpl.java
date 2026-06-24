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
import io.renren.modules.sys.dao.SysUserRepository;
import io.renren.modules.sys.dao.SysUserTokenRepository;
import io.renren.modules.sys.entity.SysMenuEntity;
import io.renren.modules.sys.entity.SysUserEntity;
import io.renren.modules.sys.entity.SysUserTokenEntity;
import io.renren.modules.sys.service.ShiroService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ShiroServiceImpl implements ShiroService {
    @Autowired
    private SysMenuRepository sysMenuRepository;
    @Autowired
    private SysUserRepository sysUserRepository;
    @Autowired
    private SysUserTokenRepository sysUserTokenRepository;

    @Override
    public Set<String> getUserPermissions(long userId) {
        List<String> permsList;

        if (userId == Constant.SUPER_ADMIN) {
            List<SysMenuEntity> menuList = sysMenuRepository.findAll();
            permsList = new ArrayList<>(menuList.size());
            for (SysMenuEntity menu : menuList) {
                permsList.add(menu.getPerms());
            }
        } else {
            permsList = sysUserRepository.queryAllPerms(userId);
        }
        Set<String> permsSet = new HashSet<>();
        for (String perms : permsList) {
            if (StringUtils.isBlank(perms)) {
                continue;
            }
            permsSet.addAll(Arrays.asList(perms.trim().split(",")));
        }
        return permsSet;
    }

    @Override
    public SysUserTokenEntity queryByToken(String token) {
        return sysUserTokenRepository.findByToken(token);
    }

    @Override
    public SysUserEntity queryUser(Long userId) {
        return sysUserRepository.findById(userId).orElse(null);
    }
}
