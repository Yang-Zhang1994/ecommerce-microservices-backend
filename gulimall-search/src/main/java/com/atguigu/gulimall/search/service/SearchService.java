package com.atguigu.gulimall.search.service;

import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;

/**
 * Product search service: query product list from Elasticsearch.
 */
public interface SearchService {

    /**
     * Search by the given search param (keyword, category, sort, hasStock, price range, brand, attrs, pagination).
     *
     * @param param search conditions from the page
     * @return structured search result view object
     */
    SearchResult search(SearchParam param);
}
