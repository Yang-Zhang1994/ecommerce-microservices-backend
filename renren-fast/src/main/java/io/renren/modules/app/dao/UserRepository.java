/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 * https://www.renren.io
 * 版权所有，侵权必究！
 */
package io.renren.modules.app.dao;

import io.renren.modules.app.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 用户 (JPA)
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    UserEntity findByMobile(String mobile);
}
