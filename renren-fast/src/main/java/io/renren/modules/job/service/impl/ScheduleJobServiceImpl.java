/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.job.service.impl;

import io.renren.common.utils.Constant;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;
import io.renren.common.exception.RRException;
import io.renren.modules.job.dao.ScheduleJobRepository;
import io.renren.modules.job.entity.ScheduleJobEntity;
import io.renren.modules.job.service.ScheduleJobService;
import io.renren.modules.job.utils.ScheduleUtils;
import org.apache.commons.lang.StringUtils;
import org.quartz.CronTrigger;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.*;

@Service("scheduleJobService")
public class ScheduleJobServiceImpl implements ScheduleJobService {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleJobServiceImpl.class);

    @Autowired
    private ScheduleJobRepository scheduleJobRepository;
    @Autowired
    private Scheduler scheduler;

    @jakarta.annotation.PostConstruct
    public void init() {
        List<ScheduleJobEntity> scheduleJobList = scheduleJobRepository.findAll();
        for (ScheduleJobEntity scheduleJob : scheduleJobList) {
            CronTrigger cronTrigger = null;
            try {
                cronTrigger = ScheduleUtils.getCronTrigger(scheduler, scheduleJob.getJobId());
            } catch (RRException e) {
                logger.warn("Could not get CronTrigger for job id {}, will recreate: {}", scheduleJob.getJobId(), e.getMessage());
                try {
                    ScheduleUtils.deleteScheduleJob(scheduler, scheduleJob.getJobId());
                } catch (Exception ignored) { }
            }
            if (cronTrigger == null) {
                try {
                    ScheduleUtils.createScheduleJob(scheduler, scheduleJob);
                } catch (RRException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof ObjectAlreadyExistsException) {
                        logger.warn("Quartz job TASK_{} already exists, deleting and recreating", scheduleJob.getJobId());
                        try {
                            ScheduleUtils.deleteScheduleJob(scheduler, scheduleJob.getJobId());
                            ScheduleUtils.createScheduleJob(scheduler, scheduleJob);
                        } catch (Exception ex) {
                            logger.error("Failed to recreate schedule job {}: {}", scheduleJob.getJobId(), ex.getMessage());
                        }
                    } else {
                        throw e;
                    }
                }
            } else {
                ScheduleUtils.updateScheduleJob(scheduler, scheduleJob);
            }
        }
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String beanName = (String) params.get("beanName");
        Pageable pageable = Query.getPageable(params);
        Specification<ScheduleJobEntity> spec = (root, query, cb) -> {
            Predicate p = cb.conjunction();
            if (StringUtils.isNotBlank(beanName)) {
                p = cb.and(p, cb.like(root.get("beanName"), "%" + beanName + "%"));
            }
            return p;
        };
        Page<ScheduleJobEntity> page = scheduleJobRepository.findAll(spec, pageable);
        return new PageUtils(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveJob(ScheduleJobEntity scheduleJob) {
        scheduleJob.setCreateTime(new Date());
        scheduleJob.setStatus(Constant.ScheduleStatus.NORMAL.getValue());
        scheduleJobRepository.save(scheduleJob);
        ScheduleUtils.createScheduleJob(scheduler, scheduleJob);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ScheduleJobEntity scheduleJob) {
        ScheduleUtils.updateScheduleJob(scheduler, scheduleJob);
        scheduleJobRepository.save(scheduleJob);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(Long[] jobIds) {
        for (Long jobId : jobIds) {
            ScheduleUtils.deleteScheduleJob(scheduler, jobId);
        }
        scheduleJobRepository.deleteAllById(Arrays.asList(jobIds));
    }

    @Override
    public int updateBatch(Long[] jobIds, int status) {
        return scheduleJobRepository.updateBatch(Arrays.asList(jobIds), status);
    }

    @Override
    public ScheduleJobEntity getById(Long jobId) {
        return scheduleJobRepository.findById(jobId).orElse(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(Long[] jobIds) {
        for (Long jobId : jobIds) {
            ScheduleUtils.run(scheduler, getById(jobId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pause(Long[] jobIds) {
        for (Long jobId : jobIds) {
            ScheduleUtils.pauseJob(scheduler, jobId);
        }
        updateBatch(jobIds, Constant.ScheduleStatus.PAUSE.getValue());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resume(Long[] jobIds) {
        for (Long jobId : jobIds) {
            ScheduleUtils.resumeJob(scheduler, jobId);
        }
        updateBatch(jobIds, Constant.ScheduleStatus.NORMAL.getValue());
    }
}
