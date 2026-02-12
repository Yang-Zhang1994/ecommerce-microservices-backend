/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package com.atguigu.common.utils;

import com.atguigu.common.xss.SQLFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Map;

/**
 * 查询参数（JPA Pageable）
 *
 * @author Mark sunlightcs@gmail.com
 */
public class Query<T> {

    /**
     * 构建 JPA Pageable（用于 Spring Data JPA）
     */
    public Pageable getPageable(Map<String, Object> params) {
        return getPageable(params, null, false);
    }

    public Pageable getPageable(Map<String, Object> params, String defaultOrderField, boolean isAsc) {
        int page = 1;
        int limit = 10;
        if (params.get(Constant.PAGE) != null) {
            page = Integer.parseInt(params.get(Constant.PAGE).toString());
        }
        if (params.get(Constant.LIMIT) != null) {
            limit = Integer.parseInt(params.get(Constant.LIMIT).toString());
        }
        String orderField = SQLFilter.sqlInject((String) params.get(Constant.ORDER_FIELD));
        String order = (String) params.get(Constant.ORDER);
        if (StringUtils.isNotEmpty(orderField) && StringUtils.isNotEmpty(order)) {
            Sort sort = Constant.ASC.equalsIgnoreCase(order) ? Sort.by(orderField).ascending() : Sort.by(orderField).descending();
            return PageRequest.of(page - 1, limit, sort);
        }
        if (StringUtils.isNotEmpty(defaultOrderField)) {
            Sort sort = isAsc ? Sort.by(defaultOrderField).ascending() : Sort.by(defaultOrderField).descending();
            return PageRequest.of(page - 1, limit, sort);
        }
        return PageRequest.of(page - 1, limit);
    }
}
