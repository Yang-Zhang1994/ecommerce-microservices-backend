/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.common.utils;

import io.renren.common.xss.SQLFilter;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Map;

/**
 * 查询参数 (JPA pagination)
 *
 * @author Mark sunlightcs@gmail.com
 */
public class Query {

    /**
     * Build Spring Data Pageable from params (for JPA).
     */
    public static Pageable getPageable(Map<String, Object> params) {
        return getPageable(params, null, false);
    }

    public static Pageable getPageable(Map<String, Object> params, String defaultOrderField, boolean isAsc) {
        int curPage = 1;
        int limit = 10;
        if (params.get(Constant.PAGE) != null) {
            curPage = Integer.parseInt((String) params.get(Constant.PAGE));
        }
        if (params.get(Constant.LIMIT) != null) {
            limit = Integer.parseInt((String) params.get(Constant.LIMIT));
        }
        String orderField = SQLFilter.sqlInject((String) params.get(Constant.ORDER_FIELD));
        String order = (String) params.get(Constant.ORDER);
        Sort sort = Sort.unsorted();
        if (StringUtils.isNotEmpty(orderField) && StringUtils.isNotEmpty(order)) {
            sort = Constant.ASC.equalsIgnoreCase(order)
                ? Sort.by(Sort.Direction.ASC, orderField)
                : Sort.by(Sort.Direction.DESC, orderField);
        } else if (StringUtils.isNotEmpty(defaultOrderField)) {
            sort = isAsc ? Sort.by(Sort.Direction.ASC, defaultOrderField) : Sort.by(Sort.Direction.DESC, defaultOrderField);
        }
        return sort.isSorted() ? PageRequest.of(curPage - 1, limit, sort) : PageRequest.of(curPage - 1, limit);
    }
}
