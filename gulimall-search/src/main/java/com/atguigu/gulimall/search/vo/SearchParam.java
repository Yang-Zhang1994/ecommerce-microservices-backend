package com.atguigu.gulimall.search.vo;

import java.util.List;

/**
 * Search request params from the search page.
 * Encapsulates all possible query conditions for product search.
 */
public class SearchParam {

    /** Full-text keyword (skuTitle, spuName, brandName, catalogName, searchable attr values) */
    private String keyword;

    /** Level-3 category IDs (supports multi-select) */
    private List<Long> catalog3Id;

    /** Sort condition, e.g. price_asc, price_desc, salecount_asc, hotscore_desc */
    private String sort;

    /** Whether to show only in-stock items (1 = yes, 0 = no) */
    private Integer hasStock;

    /** Price range, e.g. "0_500", "500_1000", "1000_" */
    private String skuPrice;

    /** Brand IDs to filter by */
    private List<Long> brandId;

    /** Attribute filters, e.g. "1_5寸:8寸", "2_4G:8G" (attrId_value or attrId_value1:value2) */
    private List<String> attrs;

    /** Page number (1-based) */
    private Integer pageNum = 1;

    /** Page size */
    private Integer pageSize = 20;

    /** Original query string for breadcrumb links (set by controller) */
    private String _queryString;

    public String get_queryString() {
        return _queryString;
    }

    public void set_queryString(String _queryString) {
        this._queryString = _queryString;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public List<Long> getCatalog3Id() {
        return catalog3Id;
    }

    public void setCatalog3Id(List<Long> catalog3Id) {
        this.catalog3Id = catalog3Id;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public Integer getHasStock() {
        return hasStock;
    }

    public void setHasStock(Integer hasStock) {
        this.hasStock = hasStock;
    }

    public String getSkuPrice() {
        return skuPrice;
    }

    public void setSkuPrice(String skuPrice) {
        this.skuPrice = skuPrice;
    }

    public List<Long> getBrandId() {
        return brandId;
    }

    public void setBrandId(List<Long> brandId) {
        this.brandId = brandId;
    }

    public List<String> getAttrs() {
        return attrs;
    }

    public void setAttrs(List<String> attrs) {
        this.attrs = attrs;
    }

    public Integer getPageNum() {
        return pageNum == null ? 1 : pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize == null ? 20 : pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
