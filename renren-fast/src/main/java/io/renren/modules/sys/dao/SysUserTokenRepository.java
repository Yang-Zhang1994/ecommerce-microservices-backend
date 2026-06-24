/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 * https://www.renren.io
 * 版权所有，侵权必究！
 */
package io.renren.modules.sys.dao;

import io.renren.modules.sys.entity.SysUserTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 系统用户Token (JPA)
 */
public interface SysUserTokenRepository extends JpaRepository<SysUserTokenEntity, Long> {

    SysUserTokenEntity findByToken(String token);
}
