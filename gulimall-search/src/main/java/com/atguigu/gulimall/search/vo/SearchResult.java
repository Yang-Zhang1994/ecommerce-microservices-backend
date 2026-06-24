package com.atguigu.gulimall.search.vo;

import com.atguigu.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.List;

/**
 * Search result view object returned from Elasticsearch.
 */
@Data
public class SearchResult {

    /**
     * Products in the current page.
     */
    private List<SkuEsModel> products;

    /**
     * Current page number (1-based).
     */
    private Integer pageNum;

    /**
     * Total record count.
     */
    private Long total;

    /**
     * Total page count.
     */
    private Integer totalPages;

    /**
     * All brands involved in current query result.
     */
    private List<BrandVo> brands;

    /**
     * All attributes involved in current query result.
     */
    private List<AttrVo> attrs;

    /**
     * All catalogs involved in current query result.
     */
    private List<CatalogVo> catalogs;

    /**
     * Page numbers for pagination nav (1, 2, 3, ... totalPages).
     */
    private List<Integer> pageNavs;

    /**
     * Breadcrumb navigation from selected attrs.
     */
    private List<NavVo> navs;

    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

    @Data
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
    }

    @Data
    public static class NavVo {
        private String navName;
        private String navValue;
        private String link;
    }
}

