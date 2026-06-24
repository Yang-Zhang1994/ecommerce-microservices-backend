/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.job.service.impl;

import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;
import io.renren.modules.job.dao.ScheduleJobLogRepository;
import io.renren.modules.job.entity.ScheduleJobLogEntity;
import io.renren.modules.job.service.ScheduleJobLogService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service("scheduleJobLogService")
public class ScheduleJobLogServiceImpl implements ScheduleJobLogService {

    @Autowired
    private ScheduleJobLogRepository scheduleJobLogRepository;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String jobIdStr = (String) params.get("jobId");
        Long jobId = null;
        if (StringUtils.isNotBlank(jobIdStr)) {
            try {
                jobId = Long.parseLong(jobIdStr);
            } catch (NumberFormatException ignored) { }
        }
        Pageable pageable = Query.getPageable(params);
        Page<ScheduleJobLogEntity> page = scheduleJobLogRepository.findFiltered(jobId, pageable);
        return new PageUtils(page);
    }

    @Override
    public ScheduleJobLogEntity getById(Long logId) {
        return scheduleJobLogRepository.findById(logId).orElse(null);
    }

    @Override
    public void save(ScheduleJobLogEntity log) {
        scheduleJobLogRepository.save(log);
    }
}
