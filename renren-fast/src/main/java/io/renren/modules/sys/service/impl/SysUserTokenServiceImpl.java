/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.sys.service.impl;

import io.renren.common.utils.R;
import io.renren.modules.sys.dao.SysUserTokenRepository;
import io.renren.modules.sys.entity.SysUserTokenEntity;
import io.renren.modules.sys.oauth2.TokenGenerator;
import io.renren.modules.sys.service.SysUserTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service("sysUserTokenService")
public class SysUserTokenServiceImpl implements SysUserTokenService {
    /** token有效时长（秒），默认7天，与 renren.jwt.expire 一致 */
    @Value("${renren.jwt.expire:604800}")
    private int expire;

    @Autowired
    private SysUserTokenRepository sysUserTokenRepository;

    @Override
    public R createToken(long userId) {
        String token = TokenGenerator.generateValue();
        Date now = new Date();
        Date expireTime = new Date(now.getTime() + expire * 1000L);

        SysUserTokenEntity tokenEntity = sysUserTokenRepository.findById(userId).orElse(null);
        if (tokenEntity == null) {
            tokenEntity = new SysUserTokenEntity();
            tokenEntity.setUserId(userId);
            tokenEntity.setToken(token);
            tokenEntity.setUpdateTime(now);
            tokenEntity.setExpireTime(expireTime);
            sysUserTokenRepository.save(tokenEntity);
        } else {
            tokenEntity.setToken(token);
            tokenEntity.setUpdateTime(now);
            tokenEntity.setExpireTime(expireTime);
            sysUserTokenRepository.save(tokenEntity);
        }

        return R.ok().put("token", token).put("expire", expire);
    }

    @Override
    public void logout(long userId) {
        String token = TokenGenerator.generateValue();
        SysUserTokenEntity tokenEntity = sysUserTokenRepository.findById(userId).orElse(null);
        if (tokenEntity != null) {
            tokenEntity.setToken(token);
            sysUserTokenRepository.save(tokenEntity);
        }
    }
}
