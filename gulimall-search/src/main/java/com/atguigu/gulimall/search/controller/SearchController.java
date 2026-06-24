package com.atguigu.gulimall.search.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.search.service.SearchService;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Search page API: product list with full query conditions (keyword, category, sort, hasStock, price, brand, attrs, pagination).
 */
@Slf4j
@RestController
@RequestMapping("/search/product")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/list")
    public R list(SearchParam param, HttpServletRequest request) {
        if (request.getQueryString() != null) {
            param.set_queryString(request.getQueryString());
        }
        // Normalize attrs: remove empty strings to avoid invalid ES filters
        if (param.getAttrs() != null && !param.getAttrs().isEmpty()) {
            param.setAttrs(param.getAttrs().stream().filter(StringUtils::hasText).collect(Collectors.toList()));
        }

        try {
            SearchResult result = searchService.search(param);
            return R.ok().put("data", result);
        } catch (Exception e) {
            log.error("Search list failed: {}", e.getMessage(), e);
            SearchResult empty = new SearchResult();
            empty.setProducts(new ArrayList<>());
            empty.setPageNum(1);
            empty.setTotal(0L);
            empty.setTotalPages(0);
            empty.setBrands(new ArrayList<>());
            empty.setCatalogs(new ArrayList<>());
            empty.setAttrs(new ArrayList<>());
            empty.setPageNavs(Collections.singletonList(1));
            empty.setNavs(new ArrayList<>());
            return R.ok().put("data", empty);
        }
    }
}
