package com.atguigu.gulimall.search.client;

import com.atguigu.common.utils.R;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

/**
 * HTTP interface for gulimall-product.
 * Used by search service to query attribute and catalog basic info.
 */
@HttpExchange
public interface ProductApi {

    /**
     * Get attribute info by id.
     * Maps to product service endpoint: /product/attr/info/{attrId}
     */
    @GetExchange("/product/attr/info/{attrId}")
    R attrInfo(@PathVariable("attrId") Long attrId);

    /**
     * Get catalog info by id.
     * Maps to product service endpoint: /product/category/info/{catId}
     */
    @GetExchange("/product/category/info/{catId}")
    R catalogInfo(@PathVariable("catId") Long catId);

    /**
     * Level-3 category ids matching search terms (name contains, case-insensitive).
     */
    @GetExchange("/product/category/match-ids")
    R categoryMatchIds(@RequestParam("terms") List<String> terms);

    /**
     * Searchable attribute metadata (searchType=1 + value_select) for facet filtering.
     */
    @GetExchange("/product/attr/search-filter-meta")
    R searchFilterMeta(@RequestParam("attrIds") List<Long> attrIds);
}

