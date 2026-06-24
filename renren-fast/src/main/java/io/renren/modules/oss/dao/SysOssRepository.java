/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 * https://www.renren.io
 * 版权所有，侵权必究！
 */
package io.renren.modules.oss.dao;

import io.renren.modules.oss.entity.SysOssEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 文件上传 (JPA)
 */
public interface SysOssRepository extends JpaRepository<SysOssEntity, Long> {
}
