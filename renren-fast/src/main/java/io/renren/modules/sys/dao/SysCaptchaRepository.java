/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 * https://www.renren.io
 * 版权所有，侵权必究！
 */
package io.renren.modules.sys.dao;

import io.renren.modules.sys.entity.SysCaptchaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 系统验证码 (JPA)
 */
public interface SysCaptchaRepository extends JpaRepository<SysCaptchaEntity, String> {
}
