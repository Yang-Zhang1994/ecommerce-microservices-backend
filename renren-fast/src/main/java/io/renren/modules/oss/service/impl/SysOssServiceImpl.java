/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.oss.service.impl;

import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;
import io.renren.modules.oss.dao.SysOssRepository;
import io.renren.modules.oss.entity.SysOssEntity;
import io.renren.modules.oss.service.SysOssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;

@Service("sysOssService")
public class SysOssServiceImpl implements SysOssService {

    @Autowired
    private SysOssRepository sysOssRepository;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = Query.getPageable(params);
        Page<SysOssEntity> page = sysOssRepository.findAll(pageable);
        return new PageUtils(page);
    }

    @Override
    public void save(SysOssEntity ossEntity) {
        sysOssRepository.save(ossEntity);
    }

    @Override
    public void removeByIds(Long[] ids) {
        sysOssRepository.deleteAllById(Arrays.asList(ids));
    }
}
