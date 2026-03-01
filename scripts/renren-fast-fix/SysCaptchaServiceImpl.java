/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.sys.service.impl;

import com.google.code.kaptcha.Producer;
import io.renren.common.exception.RRException;
import io.renren.common.utils.DateUtils;
import io.renren.modules.sys.dao.SysCaptchaRepository;
import io.renren.modules.sys.entity.SysCaptchaEntity;
import io.renren.modules.sys.service.SysCaptchaService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

@Service("sysCaptchaService")
public class SysCaptchaServiceImpl implements SysCaptchaService {
    private static final Logger log = LoggerFactory.getLogger(SysCaptchaServiceImpl.class);

    /** In-memory fallback when DB is unavailable (e.g. connection timeout). Entry: code + expireTimeMillis. */
    private static final ConcurrentHashMap<String, CaptchaEntry> MEMORY_CAPTCHA = new ConcurrentHashMap<>();

    @Autowired
    private Producer producer;
    @Autowired
    private SysCaptchaRepository sysCaptchaRepository;

    @Override
    public BufferedImage getCaptcha(String uuid) {
        if (StringUtils.isBlank(uuid)) {
            throw new RRException("uuid不能为空");
        }
        String code = producer.createText();
        long expireTimeMillis = DateUtils.addDateMinutes(new Date(), 5).getTime();

        try {
            SysCaptchaEntity captchaEntity = new SysCaptchaEntity();
            captchaEntity.setUuid(uuid);
            captchaEntity.setCode(code);
            captchaEntity.setExpireTime(new Date(expireTimeMillis));
            sysCaptchaRepository.save(captchaEntity);
        } catch (Exception e) {
            log.warn("Captcha save to DB failed, using in-memory fallback: {}", e.getMessage());
            MEMORY_CAPTCHA.put(uuid, new CaptchaEntry(code, expireTimeMillis));
        }

        return producer.createImage(code);
    }

    @Override
    public boolean validate(String uuid, String code) {
        CaptchaEntry mem = MEMORY_CAPTCHA.remove(uuid);
        if (mem != null) {
            return mem.code.equalsIgnoreCase(code) && mem.expireTimeMillis >= System.currentTimeMillis();
        }
        SysCaptchaEntity captchaEntity = sysCaptchaRepository.findById(uuid).orElse(null);
        if (captchaEntity == null) {
            return false;
        }
        sysCaptchaRepository.deleteById(uuid);
        return captchaEntity.getCode().equalsIgnoreCase(code)
                && captchaEntity.getExpireTime().getTime() >= System.currentTimeMillis();
    }

    private static final class CaptchaEntry {
        final String code;
        final long expireTimeMillis;

        CaptchaEntry(String code, long expireTimeMillis) {
            this.code = code;
            this.expireTimeMillis = expireTimeMillis;
        }
    }
}
