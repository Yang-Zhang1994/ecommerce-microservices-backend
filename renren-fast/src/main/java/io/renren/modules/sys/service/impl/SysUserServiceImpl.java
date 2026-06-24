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
import io.renren.modules.sys.dao.SysUserRepository;
import io.renren.modules.sys.entity.SysUserEntity;
import io.renren.modules.sys.service.SysRoleService;
import io.renren.modules.sys.service.SysUserRoleService;
import io.renren.modules.sys.service.SysUserService;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 系统用户
 *
 * @author Mark sunlightcs@gmail.com
 */
@Service("sysUserService")
public class SysUserServiceImpl implements SysUserService {
    @Autowired
    private SysUserRepository sysUserRepository;
    @Autowired
    private SysUserRoleService sysUserRoleService;
    @Autowired
    private SysRoleService sysRoleService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String username = (String) params.get("username");
        Long createUserId = (Long) params.get("createUserId");
        Pageable pageable = Query.getPageable(params);
        Specification<SysUserEntity> spec = (root, query, cb) -> {
            Predicate p = cb.conjunction();
            if (StringUtils.isNotBlank(username)) {
                p = cb.and(p, cb.like(root.get("username"), "%" + username + "%"));
            }
            if (createUserId != null) {
                p = cb.and(p, cb.equal(root.get("createUserId"), createUserId));
            }
            return p;
        };
        Page<SysUserEntity> page = sysUserRepository.findAll(spec, pageable);
        return new PageUtils(page);
    }

    @Override
    public List<String> queryAllPerms(Long userId) {
        return sysUserRepository.queryAllPerms(userId);
    }

    @Override
    public List<Long> queryAllMenuId(Long userId) {
        return sysUserRepository.queryAllMenuId(userId);
    }

    @Override
    public SysUserEntity getById(Long userId) {
        return sysUserRepository.findById(userId).orElse(null);
    }

    @Override
    public SysUserEntity queryByUserName(String username) {
        return sysUserRepository.findByUsername(username);
    }

    @Override
    @Transactional
    public void saveUser(SysUserEntity user) {
        user.setCreateTime(new Date());
        String salt = RandomStringUtils.randomAlphanumeric(20);
        user.setPassword(new Sha256Hash(user.getPassword(), salt).toHex());
        user.setSalt(salt);
        sysUserRepository.save(user);

        checkRole(user);
        sysUserRoleService.saveOrUpdate(user.getUserId(), user.getRoleIdList());
    }

    @Override
    @Transactional
    public void update(SysUserEntity user) {
        SysUserEntity existing = sysUserRepository.findById(user.getUserId()).orElse(null);
        if (existing == null) {
            throw new RRException("User not found");
        }
        if (user.getCreateTime() == null) {
            user.setCreateTime(existing.getCreateTime());
        }
        user.setSalt(existing.getSalt());
        if (StringUtils.isBlank(user.getPassword())) {
            user.setPassword(existing.getPassword());
        } else {
            user.setPassword(new Sha256Hash(user.getPassword(), existing.getSalt()).toHex());
        }
        sysUserRepository.save(user);

        checkRole(user);
        sysUserRoleService.saveOrUpdate(user.getUserId(), user.getRoleIdList());
    }

    @Override
    @Transactional
    public void deleteBatch(Long[] userId) {
        sysUserRepository.deleteAllById(Arrays.asList(userId));
    }

    @Override
    public boolean updatePassword(Long userId, String password, String newPassword) {
        SysUserEntity user = sysUserRepository.findById(userId).orElse(null);
        if (user == null || !user.getPassword().equals(password)) {
            return false;
        }
        user.setPassword(newPassword);
        sysUserRepository.save(user);
        return true;
    }

    private void checkRole(SysUserEntity user) {
        if (user.getRoleIdList() == null || user.getRoleIdList().size() == 0) {
            return;
        }
        if (user.getCreateUserId() == Constant.SUPER_ADMIN) {
            return;
        }
        List<Long> roleIdList = sysRoleService.queryRoleIdList(user.getCreateUserId());
        if (!roleIdList.containsAll(user.getRoleIdList())) {
            throw new RRException("新增用户所选角色，不是本人创建");
        }
    }
}
