package com.atguigu.gulimall.search.service.impl;

import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.AttrValueSort;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.search.client.BrandApi;
import com.atguigu.gulimall.search.client.ProductApi;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfiguration;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.service.SearchService;
import com.atguigu.gulimall.search.util.SearchKeywordSynonyms;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Collections;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mall search service implementation.
 * Query DSL: multi_match keyword (skuTitle, brandName, catalogName, attrs) + filter, sort, paginate, highlight, aggregations.
 */
@Service
public class SearchServiceImpl implements SearchService {

    private final RestHighLevelClient esClient;
    private final ProductApi productApi;
    private final BrandApi brandApi;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SearchServiceImpl(RestHighLevelClient esRestClient, ProductApi productApi, BrandApi brandApi) {
        this.esClient = esRestClient;
        this.productApi = productApi;
        this.brandApi = brandApi;
    }

    @Override
    public SearchResult search(SearchParam param) {
        SearchRequest searchRequest = buildSearchRequest(param);
        try {
            SearchResponse response = esClient.search(searchRequest, GulimallElasticSearchConfiguration.COMMON_OPTIONS);
            return buildSearchResult(response, param);
        } catch (Exception e) {
            throw new RuntimeException("Search failed", e);
        }
    }

    /**
     * Build search result from ES response.
     * Includes products, brands, catalogs, attrs (from aggregations), pagination, breadcrumb.
     */
    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {
        SearchResult result = new SearchResult();
        SearchHits hits = response.getHits();

        // 1. Products
        List<SkuEsModel> products = new ArrayList<>();
        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                try {
                    SkuEsModel model = objectMapper.readValue(hit.getSourceAsString(), SkuEsModel.class);
                    if (StringUtils.hasText(param.getKeyword())) {
                        HighlightField skuTitleField = hit.getHighlightFields().get("skuTitle");
                        if (skuTitleField != null && skuTitleField.getFragments() != null && skuTitleField.getFragments().length > 0) {
                            model.setSkuTitle(skuTitleField.getFragments()[0].string());
                        }
                    }
                    products.add(model);
                } catch (Exception ignored) {
                }
            }
        }
        enrichProductsFromDb(products);
        result.setProducts(products);

        // Aggregations at top level (scoped by query)
        org.elasticsearch.search.aggregations.Aggregations subAggs = response.getAggregations();

        // 2. Attrs from aggregation (group by attrId only; names from DB in enrichAttrVosFromProductDb)
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        Map<Long, SearchResult.AttrVo> attrById = new LinkedHashMap<>();
        if (subAggs != null && subAggs.get("attr_agg") != null) {
            ParsedNested attrsAgg = subAggs.get("attr_agg");
            if (attrsAgg.getAggregations().get("attr_id_agg") != null) {
                Terms attrIdAgg = attrsAgg.getAggregations().get("attr_id_agg");
                for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
                    long attrId = bucket.getKeyAsNumber().longValue();
                    if (attrId == 0) continue;
                    SearchResult.AttrVo attrVo = attrById.computeIfAbsent(attrId, id -> {
                        SearchResult.AttrVo vo = new SearchResult.AttrVo();
                        vo.setAttrId(id);
                        vo.setAttrValue(new ArrayList<>());
                        return vo;
                    });
                    ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attr_value_agg");
                    if (attrValueAgg != null) {
                        LinkedHashSet<String> merged = new LinkedHashSet<>(attrVo.getAttrValue());
                        attrValueAgg.getBuckets().stream()
                                .map(Terms.Bucket::getKeyAsString)
                                .filter(StringUtils::hasText)
                                .map(String::trim)
                                .forEach(merged::add);
                        List<String> values = new ArrayList<>(merged);
                        sortAttrValuesAsc(values, null);
                        attrVo.setAttrValue(values);
                    }
                }
                attrVos.addAll(attrById.values());
            }
        }
        // Keep only attr values that return at least one product when used as filter (split by ; for multi-values)
        try {
            attrVos = filterAttrsWithHits(attrVos, param);
        } catch (Exception e) {
            // Keep original aggregation result on any failure so search page still shows products
        }
        // North America-style faceted filtering:
        // keep the same attribute dimension visible for multi-select,
        // but hide already selected values from its candidate list.
        Map<Long, Set<String>> selectedAttrValues = extractSelectedAttrValues(param.getAttrs());
        if (!selectedAttrValues.isEmpty()) {
            List<SearchResult.AttrVo> filteredAttrVos = new ArrayList<>();
            for (SearchResult.AttrVo attrVo : attrVos) {
                if (attrVo == null || attrVo.getAttrId() == null || CollectionUtils.isEmpty(attrVo.getAttrValue())) {
                    filteredAttrVos.add(attrVo);
                    continue;
                }
                Set<String> selectedValues = selectedAttrValues.get(attrVo.getAttrId());
                if (CollectionUtils.isEmpty(selectedValues)) {
                    filteredAttrVos.add(attrVo);
                    continue;
                }
                List<String> remainedValues = attrVo.getAttrValue().stream()
                        .filter(StringUtils::hasText)
                        .filter(v -> !selectedValues.contains(v.trim()))
                        .collect(Collectors.toList());
                if (!remainedValues.isEmpty()) {
                    SearchResult.AttrVo copy = new SearchResult.AttrVo();
                    copy.setAttrId(attrVo.getAttrId());
                    copy.setAttrName(attrVo.getAttrName());
                    copy.setAttrValue(remainedValues);
                    filteredAttrVos.add(copy);
                }
            }
            attrVos = filteredAttrVos;
        }
        enrichAttrVosFromProductDb(attrVos);
        result.setAttrs(attrVos);

        // 3. Brands from aggregation (only brands in search result, excluding brands already filtered in URL)
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        if (subAggs != null && subAggs.get("brand_agg") != null) {
            ParsedLongTerms brandAgg = subAggs.get("brand_agg");
            for (Terms.Bucket bucket : brandAgg.getBuckets()) {
                SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
                brandVo.setBrandId(bucket.getKeyAsNumber().longValue());
                brandVos.add(brandVo);
            }
        }
        enrichBrandVosFromDb(brandVos);

        // 如果 URL 中已经选择了品牌，则这些品牌会出现在面包屑中，不再出现在可选筛选栏
        if (!CollectionUtils.isEmpty(param.getBrandId())) {
            List<Long> selectedBrandIds = param.getBrandId();
            brandVos = brandVos.stream()
                    .filter(b -> !selectedBrandIds.contains(b.getBrandId()))
                    .collect(Collectors.toList());
        }
        result.setBrands(brandVos);

        // 4. Catalogs from aggregation (only catalogs in search result; current selected catalog shows only in breadcrumb)
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        if (subAggs != null && subAggs.get("catalog_agg") != null) {
            ParsedLongTerms catalogAgg = subAggs.get("catalog_agg");
            for (Terms.Bucket bucket : catalogAgg.getBuckets()) {
                SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
                catalogVo.setCatalogId(bucket.getKeyAsNumber().longValue());
                catalogVos.add(catalogVo);
            }
        }
        enrichCatalogVosFromDb(catalogVos);
        // 如果当前已经按某个三级分类检索，则该分类仅在面包屑中显示，不再在分类筛选列表中出现
        if (!CollectionUtils.isEmpty(param.getCatalog3Id())) {
            List<Long> selectedCatalogIds = param.getCatalog3Id();
            catalogVos = catalogVos.stream()
                    .filter(c -> !selectedCatalogIds.contains(c.getCatalogId()))
                    .collect(Collectors.toList());
        }
        result.setCatalogs(catalogVos);

        // 5. Pagination
        result.setPageNum(param.getPageNum());
        long total = hits.getTotalHits().value;
        result.setTotal(total);
        int pageSize = param.getPageSize();
        int totalPages = (int) total % pageSize == 0
                ? (int) total / pageSize
                : (int) total / pageSize + 1;
        result.setTotalPages(totalPages);

        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);

        // 6. Breadcrumb (navs): attrs + brand + catalog
        // Prefer attr names from ES aggregation/hits to avoid fallback text like "属性13".
        Map<Long, String> attrNameMap = buildAttrNameMap(attrVos);
        Map<Long, String> brandNameMap = buildBrandNameMap(brandVos, products);
        Map<Long, String> catalogNameMap = buildCatalogNameMap(catalogVos, products);
        result.setNavs(buildNavs(param, attrNameMap, brandNameMap, catalogNameMap));

        return result;
    }

    @Cacheable(value = "attr", key = "'attrInfo:' + #attrId")
    protected String getAttrName(Long attrId) {
        String attrName = "Attribute";
        try {
            R r = productApi.attrInfo(attrId);
            if (r != null && r.getCode() != null && r.getCode() == 0) {
                Object attrObj = r.get("attr");
                if (attrObj instanceof Map<?, ?> map) {
                    Object nameObj = map.get("attrName");
                    if (nameObj != null) {
                        attrName = String.valueOf(nameObj);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return attrName;
    }

    @Cacheable(value = "brand", key = "'brandInfos:' + #brandIds")
    @SuppressWarnings("unchecked")
    protected List<Map<String, Object>> getBrandInfos(List<Long> brandIds) {
        return loadBrandInfosUncached(brandIds);
    }

    private List<Map<String, Object>> loadBrandInfosUncached(List<Long> brandIds) {
        try {
            R r = brandApi.infos(brandIds);
            if (r != null && r.getCode() != null && r.getCode() == 0) {
                Object brandsObj = r.get("brands");
                if (brandsObj instanceof List<?> list && !list.isEmpty()) {
                    return objectMapper.convertValue(
                            list, new TypeReference<List<Map<String, Object>>>() {});
                }
            }
        } catch (Exception ignored) {
        }
        return new ArrayList<>();
    }

    @Cacheable(value = "catalog", key = "'catalogInfo:' + #catalogId")
    protected String getCatalogName(Long catalogId) {
        try {
            R r = productApi.catalogInfo(catalogId);
            if (r != null && r.getCode() != null && r.getCode() == 0) {
                Object catObj = r.get("data");
                if (catObj == null) {
                    catObj = r.get("category");
                }
                if (catObj instanceof Map<?, ?> map) {
                    Object nameObj = map.get("name");
                    if (nameObj != null && StringUtils.hasText(String.valueOf(nameObj))) {
                        return String.valueOf(nameObj).trim();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Build breadcrumb navigation based on selected attrs, brands, and catalog.
     */
    private List<SearchResult.NavVo> buildNavs(SearchParam param, Map<Long, String> attrNameMap, Map<Long, String> brandNameMap, Map<Long, String> catalogNameMap) {
        List<SearchResult.NavVo> navs = new ArrayList<>();
        if (param == null) {
            return navs;
        }
        String originalQs = param.get_queryString();
        // 1. Attr navs
        if (!CollectionUtils.isEmpty(param.getAttrs())) {
            List<String> attrs = param.getAttrs().stream()
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
            for (String attr : attrs) {
                int idx = attr.indexOf('_');
                if (idx <= 0) {
                    continue;
                }
                String attrIdStr = attr.substring(0, idx).trim();
                String attrValueStr = attr.substring(idx + 1);
                if (!StringUtils.hasText(attrIdStr)) continue;
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                navVo.setNavValue(attrValueStr);

                // 1. Resolve attr name: ES aggregation/hits first, then product service fallback
                String attrName = "Attribute";
                try {
                    long attrId = Long.parseLong(attrIdStr);
                    String fromMap = attrNameMap == null ? null : attrNameMap.get(attrId);
                    if (StringUtils.hasText(fromMap)) {
                        attrName = fromMap.trim();
                    } else {
                        attrName = getAttrName(attrId);
                    }
                } catch (Exception ignored) {
                }
                navVo.setNavName(attrName);

                // 2. Build link: remove current attr filter from original query string
                if (StringUtils.hasText(originalQs)) {
                    String newQs = removeSingleQueryParam(originalQs, "attrs", attr);
                    navVo.setLink("/search" + (StringUtils.hasText(newQs) ? ("?" + newQs) : "") + "#search-results-rig-tab");
                } else {
                    navVo.setLink("/search#search-results-rig-tab");
                }
                navs.add(navVo);
            }
        }
        // 2. Brand navs (支持取消)
        if (!CollectionUtils.isEmpty(param.getBrandId())) {
            List<Long> brandIds = param.getBrandId();
            Map<Long, String> resolvedBrandNames = new LinkedHashMap<>();
            try {
                List<Map<String, Object>> brands = getBrandInfos(brandIds);
                for (Map<String, Object> map : brands) {
                    Object idObj = map.get("brandId");
                    Object nameObj = map.get("name");
                    if (idObj == null || nameObj == null) continue;
                    Long brandId = Long.valueOf(String.valueOf(idObj));
                    String brandName = String.valueOf(nameObj).trim();
                    if (StringUtils.hasText(brandName)) {
                        resolvedBrandNames.put(brandId, brandName);
                    }
                }
            } catch (Exception ignored) {
            }
            for (Long brandId : brandIds) {
                if (brandId == null) continue;
                String brandName = resolvedBrandNames.get(brandId);
                if (!StringUtils.hasText(brandName) && brandNameMap != null) {
                    brandName = brandNameMap.get(brandId);
                }
                if (!StringUtils.hasText(brandName)) {
                    brandName = String.valueOf(brandId);
                }
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                navVo.setNavName("Brand");
                navVo.setNavValue(brandName);
                if (StringUtils.hasText(originalQs)) {
                    String newQs = removeSingleQueryParam(originalQs, "brandId", String.valueOf(brandId));
                    navVo.setLink("/search" + (StringUtils.hasText(newQs) ? ("?" + newQs) : "") + "#search-results-rig-tab");
                } else {
                    navVo.setLink("/search#search-results-rig-tab");
                }
                navs.add(navVo);
            }
        }

        // 3. Catalog nav (支持取消，和品牌/属性一致)
        if (!CollectionUtils.isEmpty(param.getCatalog3Id())) {
            for (Long catalogId : param.getCatalog3Id()) {
                if (catalogId == null) continue;
                try {
                    String catalogName = getCatalogName(catalogId);
                    if (!StringUtils.hasText(catalogName) && catalogNameMap != null) {
                        catalogName = catalogNameMap.get(catalogId);
                    }
                    SearchResult.NavVo navVo = new SearchResult.NavVo();
                    navVo.setNavName("Category");
                    navVo.setNavValue(StringUtils.hasText(catalogName) ? catalogName : String.valueOf(catalogId));
                    if (StringUtils.hasText(originalQs)) {
                        String newQs = removeSingleQueryParam(originalQs, "catalog3Id", String.valueOf(catalogId));
                        navVo.setLink("/search" + (StringUtils.hasText(newQs) ? ("?" + newQs) : "") + "#search-results-rig-tab");
                    } else {
                        navVo.setLink("/search#search-results-rig-tab");
                    }
                    navs.add(navVo);
                } catch (Exception ignored) {
                }
            }
        }

        return navs;
    }

    /**
     * Build attrId -> attrName map from current ES response.
     * Aggregation names are preferred; product-hit attrs are used as fallback.
     */
    private Map<Long, String> buildAttrNameMap(List<SearchResult.AttrVo> attrVos) {
        Map<Long, String> attrNameMap = new LinkedHashMap<>();
        if (!CollectionUtils.isEmpty(attrVos)) {
            for (SearchResult.AttrVo attrVo : attrVos) {
                if (attrVo == null || attrVo.getAttrId() == null) continue;
                if (StringUtils.hasText(attrVo.getAttrName())) {
                    attrNameMap.put(attrVo.getAttrId(), attrVo.getAttrName().trim());
                }
            }
        }
        return attrNameMap;
    }

    /**
     * Keep only searchType=1 attrs and facet values that exist in pms_attr.value_select and current ES hits.
     */
    private void enrichAttrVosFromProductDb(List<SearchResult.AttrVo> attrVos) {
        if (CollectionUtils.isEmpty(attrVos)) {
            return;
        }
        List<Long> attrIds = attrVos.stream()
                .map(SearchResult.AttrVo::getAttrId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (attrIds.isEmpty()) {
            return;
        }
        try {
            R r = productApi.searchFilterMeta(attrIds);
            if (r == null || r.getCode() == null || r.getCode() != 0) {
                return;
            }
            Object dataObj = r.get("data");
            if (!(dataObj instanceof List<?> list) || list.isEmpty()) {
                attrVos.clear();
                return;
            }
            List<Map<String, Object>> metaList = objectMapper.convertValue(
                    list, new TypeReference<List<Map<String, Object>>>() {});
            Map<Long, Map<String, Object>> metaById = new LinkedHashMap<>();
            for (Map<String, Object> meta : metaList) {
                if (meta == null || meta.get("attrId") == null) {
                    continue;
                }
                Long id = Long.valueOf(String.valueOf(meta.get("attrId")));
                metaById.put(id, meta);
            }
            List<SearchResult.AttrVo> resolved = new ArrayList<>();
            for (SearchResult.AttrVo attrVo : attrVos) {
                if (attrVo == null || attrVo.getAttrId() == null) {
                    continue;
                }
                Map<String, Object> meta = metaById.get(attrVo.getAttrId());
                if (meta == null) {
                    continue;
                }
                Object nameObj = meta.get("attrName");
                if (nameObj != null && StringUtils.hasText(String.valueOf(nameObj))) {
                    attrVo.setAttrName(String.valueOf(nameObj).trim());
                }
                Set<String> allowed = new LinkedHashSet<>();
                Object allowedObj = meta.get("allowedValues");
                if (allowedObj instanceof List<?> allowedList) {
                    for (Object v : allowedList) {
                        if (v != null && StringUtils.hasText(String.valueOf(v))) {
                            allowed.add(String.valueOf(v).trim());
                        }
                    }
                }
                if (allowed.isEmpty()) {
                    continue;
                }
                List<String> esValues = attrVo.getAttrValue() != null ? attrVo.getAttrValue() : List.of();
                List<String> filtered = esValues.stream()
                        .filter(StringUtils::hasText)
                        .map(String::trim)
                        .filter(allowed::contains)
                        .collect(Collectors.toList());
                if (filtered.isEmpty()) {
                    continue;
                }
                sortAttrValuesAsc(filtered, attrVo.getAttrName());
                attrVo.setAttrValue(filtered);
                resolved.add(attrVo);
            }
            attrVos.clear();
            attrVos.addAll(resolved);
        } catch (Exception ignored) {
        }
    }

    /** Fill brandName on search hits from pms_brand (ES stores brandId only). */
    private void enrichProductsFromDb(List<SkuEsModel> products) {
        if (CollectionUtils.isEmpty(products)) {
            return;
        }
        List<Long> brandIds = products.stream()
                .map(SkuEsModel::getBrandId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (brandIds.isEmpty()) {
            return;
        }
        try {
            List<Map<String, Object>> brands = loadBrandInfosUncached(brandIds);
            Map<Long, String> nameById = new LinkedHashMap<>();
            for (Map<String, Object> map : brands) {
                if (map == null || map.get("brandId") == null) {
                    continue;
                }
                Long brandId = Long.valueOf(String.valueOf(map.get("brandId")));
                Object nameObj = map.get("name");
                if (nameObj != null && StringUtils.hasText(String.valueOf(nameObj))) {
                    nameById.put(brandId, String.valueOf(nameObj).trim());
                }
            }
            for (SkuEsModel product : products) {
                if (product == null || product.getBrandId() == null) {
                    continue;
                }
                String name = nameById.get(product.getBrandId());
                if (StringUtils.hasText(name)) {
                    product.setBrandName(name);
                }
                if (product.getCatalogId() != null) {
                    String catalogName = getCatalogName(product.getCatalogId());
                    if (StringUtils.hasText(catalogName)) {
                        product.setCatalogName(catalogName);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    /** Fill catalogName on facet rows from pms_category (ES stores catalogId only). */
    private void enrichCatalogVosFromDb(List<SearchResult.CatalogVo> catalogVos) {
        if (CollectionUtils.isEmpty(catalogVos)) {
            return;
        }
        for (SearchResult.CatalogVo catalogVo : catalogVos) {
            if (catalogVo == null || catalogVo.getCatalogId() == null) {
                continue;
            }
            String name = getCatalogName(catalogVo.getCatalogId());
            if (StringUtils.hasText(name)) {
                catalogVo.setCatalogName(name);
            }
        }
    }

    private static long parseAttrIdFromAggKey(String key) {
        if (!StringUtils.hasText(key)) {
            return 0;
        }
        String idPart = key;
        int pipe = key.indexOf('|');
        if (pipe > 0) {
            idPart = key.substring(0, pipe);
        }
        try {
            return Long.parseLong(idPart.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** Logo/name for brand facets from pms_brand (ES stores brandId only). */
    private void enrichBrandVosFromDb(List<SearchResult.BrandVo> brandVos) {
        if (CollectionUtils.isEmpty(brandVos)) {
            return;
        }
        List<Long> brandIds = brandVos.stream()
                .map(SearchResult.BrandVo::getBrandId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (brandIds.isEmpty()) {
            return;
        }
        try {
            // Fresh logos from DB (avoid stale @Cacheable brandInfos after admin updates logo).
            List<Map<String, Object>> brands = loadBrandInfosUncached(brandIds);
            Map<Long, String> logoById = new LinkedHashMap<>();
            Map<Long, String> nameById = new LinkedHashMap<>();
            for (Map<String, Object> map : brands) {
                if (map == null) {
                    continue;
                }
                Object idObj = map.get("brandId");
                if (idObj == null) {
                    continue;
                }
                Long brandId = Long.valueOf(String.valueOf(idObj));
                Object nameObj = map.get("name");
                if (nameObj != null && StringUtils.hasText(String.valueOf(nameObj))) {
                    nameById.put(brandId, String.valueOf(nameObj).trim());
                }
                Object logoObj = map.get("logo");
                if (logoObj != null && StringUtils.hasText(String.valueOf(logoObj))) {
                    logoById.put(brandId, String.valueOf(logoObj).trim());
                }
            }
            List<SearchResult.BrandVo> resolved = new ArrayList<>();
            for (SearchResult.BrandVo brandVo : brandVos) {
                if (brandVo == null || brandVo.getBrandId() == null) {
                    continue;
                }
                Long id = brandVo.getBrandId();
                String logo = logoById.get(id);
                if (!StringUtils.hasText(logo)) {
                    continue;
                }
                brandVo.setBrandImg(logo);
                String name = nameById.get(id);
                if (StringUtils.hasText(name)) {
                    brandVo.setBrandName(name);
                }
                resolved.add(brandVo);
            }
            brandVos.clear();
            brandVos.addAll(resolved);
        } catch (Exception ignored) {
        }
    }

    private Map<Long, String> buildBrandNameMap(List<SearchResult.BrandVo> brandVos, List<SkuEsModel> products) {
        Map<Long, String> brandNameMap = new LinkedHashMap<>();
        if (!CollectionUtils.isEmpty(brandVos)) {
            for (SearchResult.BrandVo brandVo : brandVos) {
                if (brandVo == null || brandVo.getBrandId() == null || !StringUtils.hasText(brandVo.getBrandName())) continue;
                brandNameMap.put(brandVo.getBrandId(), brandVo.getBrandName().trim());
            }
        }
        if (!CollectionUtils.isEmpty(products)) {
            for (SkuEsModel product : products) {
                if (product == null || product.getBrandId() == null || !StringUtils.hasText(product.getBrandName())) continue;
                brandNameMap.putIfAbsent(product.getBrandId(), product.getBrandName().trim());
            }
        }
        return brandNameMap;
    }

    private Map<Long, String> buildCatalogNameMap(List<SearchResult.CatalogVo> catalogVos, List<SkuEsModel> products) {
        Map<Long, String> catalogNameMap = new LinkedHashMap<>();
        if (!CollectionUtils.isEmpty(catalogVos)) {
            for (SearchResult.CatalogVo catalogVo : catalogVos) {
                if (catalogVo == null || catalogVo.getCatalogId() == null || !StringUtils.hasText(catalogVo.getCatalogName())) continue;
                catalogNameMap.put(catalogVo.getCatalogId(), catalogVo.getCatalogName().trim());
            }
        }
        if (!CollectionUtils.isEmpty(products)) {
            for (SkuEsModel product : products) {
                if (product == null || product.getCatalogId() == null || !StringUtils.hasText(product.getCatalogName())) continue;
                catalogNameMap.putIfAbsent(product.getCatalogId(), product.getCatalogName().trim());
            }
        }
        return catalogNameMap;
    }

    private static Map<Long, Set<String>> extractSelectedAttrValues(List<String> attrs) {
        Map<Long, Set<String>> selected = new LinkedHashMap<>();
        if (CollectionUtils.isEmpty(attrs)) return selected;
        for (String attr : attrs) {
            if (!StringUtils.hasText(attr) || !attr.contains("_")) continue;
            int idx = attr.indexOf('_');
            if (idx <= 0) continue;
            String idStr = attr.substring(0, idx).trim();
            String valueStr = attr.substring(idx + 1).trim();
            if (!StringUtils.hasText(idStr) || !StringUtils.hasText(valueStr)) continue;
            try {
                Long attrId = Long.parseLong(idStr);
                selected.computeIfAbsent(attrId, k -> new LinkedHashSet<>()).add(valueStr);
            } catch (NumberFormatException ignored) {
            }
        }
        return selected;
    }

    /**
     * Remove exactly one matching query pair (decoded key/value compare) and keep all other filters.
     */
    private static String removeSingleQueryParam(String queryString, String key, String decodedValue) {
        if (!StringUtils.hasText(queryString)) return "";
        String[] parts = queryString.split("&");
        List<String> kept = new ArrayList<>();
        boolean removed = false;
        for (String part : parts) {
            if (!StringUtils.hasText(part)) continue;
            int eq = part.indexOf('=');
            String rawKey = eq >= 0 ? part.substring(0, eq) : part;
            String rawValue = eq >= 0 ? part.substring(eq + 1) : "";
            String decodedKey = decodeQueryComponent(rawKey);
            String decodedPartValue = decodeQueryComponent(rawValue);
            if (!removed && key.equals(decodedKey) && String.valueOf(decodedValue).equals(decodedPartValue)) {
                removed = true;
                continue;
            }
            kept.add(part);
        }
        return String.join("&", kept);
    }

    private static String decodeQueryComponent(String raw) {
        if (raw == null) return "";
        try {
            return URLDecoder.decode(raw, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return raw;
        }
    }

    /**
     * Build search request: match + filter, sort, paginate, highlight, aggregations.
     */
    private SearchRequest buildSearchRequest(SearchParam param) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 1.1 bool-must: keyword (multi-field + nested attr values)
        if (StringUtils.hasText(param.getKeyword())) {
            boolQuery.must(buildKeywordQuery(param.getKeyword()));
        }

        // 1.2 bool-filter: catalog
        if (!CollectionUtils.isEmpty(param.getCatalog3Id())) {
            boolQuery.filter(QueryBuilders.termsQuery("catalogId", param.getCatalog3Id()));
        }

        // 1.2 bool-filter: brandId
        if (!CollectionUtils.isEmpty(param.getBrandId())) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }

        // 1.2 bool-filter: attrs (group by attrId; value can be exact or part of "val1;val2" in ES)
        if (!CollectionUtils.isEmpty(param.getAttrs())) {
            Map<String, List<String>> attrIdToValues = new LinkedHashMap<>();
            for (String attr : param.getAttrs()) {
                if (!StringUtils.hasText(attr) || !attr.contains("_")) continue;
                int firstUnderscore = attr.indexOf('_');
                String attrIdStr = attr.substring(0, firstUnderscore).trim();
                String valuePart = attr.substring(firstUnderscore + 1).trim();
                if (!StringUtils.hasText(valuePart)) continue;
                attrIdToValues.computeIfAbsent(attrIdStr, k -> new ArrayList<>()).add(valuePart);
            }
            for (Map.Entry<String, List<String>> e : attrIdToValues.entrySet()) {
                String attrIdStr = e.getKey();
                List<String> values = e.getValue();
                if (values.isEmpty()) continue;
                BoolQueryBuilder nestedBool = QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("attrs.attrId", Long.parseLong(attrIdStr)));
                // Match attrValue: exact term OR wildcard *value* (ES may store "2018;2019" as one string)
                BoolQueryBuilder valueBool = QueryBuilders.boolQuery();
                for (String value : values) {
                    valueBool.should(QueryBuilders.termQuery("attrs.attrValue.keyword", value));
                    valueBool.should(QueryBuilders.wildcardQuery("attrs.attrValue.keyword", "*" + escapeWildcard(value) + "*"));
                }
                valueBool.minimumShouldMatch(1);
                nestedBool.must(valueBool);
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBool, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }

        // 1.2 bool-filter: hasStock
        if (param.getHasStock() != null && param.getHasStock() == 1) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", true));
        }

        // 1.2 bool-filter: skuPrice
        if (StringUtils.hasText(param.getSkuPrice())) {
            String range = param.getSkuPrice().trim();
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            if (range.contains("_")) {
                String[] parts = range.split("_");
                if (parts.length >= 1 && !parts[0].isEmpty()) {
                    try {
                        rangeQuery.gte(Double.parseDouble(parts[0]));
                    } catch (NumberFormatException ignored) {
                    }
                }
                if (parts.length >= 2 && !parts[1].isEmpty()) {
                    try {
                        rangeQuery.lte(Double.parseDouble(parts[1]));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            boolQuery.filter(rangeQuery);
        }

        sourceBuilder.query(boolQuery);

        // 2. Sort
        String sort = param.getSort();
        if (StringUtils.hasText(sort)) {
            String[] sortFields = sort.split("_");
            SortOrder order = sortFields.length > 1 && "asc".equalsIgnoreCase(sortFields[1])
                    ? SortOrder.ASC
                    : SortOrder.DESC;
            String field = sortFields[0];
            switch (field) {
                case "price", "skuPrice" -> sourceBuilder.sort("skuPrice", order);
                case "salecount", "saleCount" -> sourceBuilder.sort("saleCount", order);
                case "hotscore", "hotScore" -> sourceBuilder.sort("hotScore", order);
                default -> sourceBuilder.sort("hotScore", SortOrder.DESC);
            }
        } else {
            sourceBuilder.sort("hotScore", SortOrder.DESC);
        }

        // 3. Pagination
        int pageNum = param.getPageNum();
        int pageSize = Math.min(50, Math.max(1, param.getPageSize()));
        sourceBuilder.from((pageNum - 1) * pageSize);
        sourceBuilder.size(pageSize);

        // 4. Highlight matched text fields (title shown on cards; brand/catalog optional)
        if (StringUtils.hasText(param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.field("spuName");
            highlightBuilder.field("brandName");
            highlightBuilder.field("catalogName");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            sourceBuilder.highlighter(highlightBuilder);
        }

        // 5. Aggregations (run on query result set by default)
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg")
                .field("brandId")
                .size(50);
        sourceBuilder.aggregation(brandAgg);

        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalog_agg")
                .field("catalogId")
                .size(20);
        sourceBuilder.aggregation(catalogAgg);

        NestedAggregationBuilder attrAgg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg")
                .field("attrs.attrId")
                .size(100);
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue.keyword").size(50));
        attrAgg.subAggregation(attrIdAgg);
        sourceBuilder.aggregation(attrAgg);

        SearchRequest searchRequest = new SearchRequest(EsConstant.PRODUCT_INDEX);
        searchRequest.source(sourceBuilder);
        return searchRequest;
    }

    /** Escape * and ? for ES wildcard query so value is treated literally. */
    private static String escapeWildcard(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("*", "\\*").replace("?", "\\?");
    }

    /**
     * Sort attribute values ascending: numeric values (e.g. 4, 5, 6.7, 2018, 2021) by number,
     * others by string. So "Screen Size" / "发行年份" etc. show in natural order.
     */
    private static void sortAttrValuesAsc(List<String> values, String attrName) {
        if (values == null || values.size() <= 1) {
            return;
        }
        values.sort(AttrValueSort.forAttrName(attrName));
    }

    /**
     * Keep only attr values that return at least one product when used as filter.
     * Values like "2018;2019" are split by ";"; each part is checked and only parts with hits are kept.
     */
    private List<SearchResult.AttrVo> filterAttrsWithHits(List<SearchResult.AttrVo> attrVos, SearchParam param) {
        if (CollectionUtils.isEmpty(attrVos) || param == null) return attrVos;
        BoolQueryBuilder baseBool = buildBaseBoolQuery(param);
        List<SearchResult.AttrVo> out = new ArrayList<>();
        for (SearchResult.AttrVo attrVo : attrVos) {
            if (attrVo == null || attrVo.getAttrId() == null) continue;
            List<String> rawValues = attrVo.getAttrValue();
            if (rawValues == null) continue;
            List<String> kept = new ArrayList<>();
            java.util.Set<String> seen = new java.util.LinkedHashSet<>();
            for (String raw : rawValues) {
                for (String part : raw.split("[;；]")) {
                    String v = part.trim();
                    if (!StringUtils.hasText(v) || !seen.add(v)) continue;
                    if (hasHitsForAttrValue(baseBool, attrVo.getAttrId().longValue(), v)) {
                        kept.add(v);
                    }
                }
            }
            if (!kept.isEmpty()) {
                sortAttrValuesAsc(kept, attrVo.getAttrName());
                SearchResult.AttrVo copy = new SearchResult.AttrVo();
                copy.setAttrId(attrVo.getAttrId());
                copy.setAttrName(attrVo.getAttrName());
                copy.setAttrValue(kept);
                out.add(copy);
            }
        }
        return out;
    }

    /** Keyword: skuTitle, spuName, brandName, catalogName, attrs; synonyms + catalogId fallback for category terms. */
    private BoolQueryBuilder buildKeywordQuery(String keyword) {
        String kw = keyword.trim();
        List<String> searchTerms = SearchKeywordSynonyms.expand(kw);
        BoolQueryBuilder keywordBool = QueryBuilders.boolQuery();
        LinkedHashSet<String> seenTerms = new LinkedHashSet<>();
        for (String term : searchTerms) {
            if (!StringUtils.hasText(term)) {
                continue;
            }
            String t = term.trim();
            if (!seenTerms.add(t.toLowerCase(Locale.ROOT))) {
                continue;
            }
            keywordBool.should(buildTextMatchClause(t));
        }
        List<Long> catalogIds = resolveCatalogIdsByTerms(searchTerms);
        if (!catalogIds.isEmpty()) {
            keywordBool.should(QueryBuilders.termsQuery("catalogId", catalogIds).boost(2.0f));
        }
        keywordBool.minimumShouldMatch(1);
        return keywordBool;
    }

    private BoolQueryBuilder buildTextMatchClause(String term) {
        BoolQueryBuilder textBool = QueryBuilders.boolQuery();
        textBool.should(QueryBuilders.multiMatchQuery(term)
                .field("skuTitle", 3.0f)
                .field("spuName", 2.5f)
                .field("brandName", 2.0f)
                .field("catalogName", 1.5f)
                .type(MultiMatchQueryBuilder.Type.BEST_FIELDS)
                .operator(org.elasticsearch.index.query.Operator.OR));
        textBool.should(QueryBuilders.nestedQuery(
                "attrs",
                QueryBuilders.matchQuery("attrs.attrValue", term),
                ScoreMode.Avg));
        textBool.minimumShouldMatch(1);
        return textBool;
    }

    private List<Long> resolveCatalogIdsByTerms(List<String> terms) {
        if (CollectionUtils.isEmpty(terms)) {
            return List.of();
        }
        try {
            R r = productApi.categoryMatchIds(terms);
            if (r == null || r.get("data") == null) {
                return List.of();
            }
            List<Long> ids = objectMapper.convertValue(r.get("data"), new TypeReference<List<Long>>() {});
            return ids != null ? ids : List.of();
        } catch (Exception ignored) {
            return List.of();
        }
    }

    /** Base bool query without attrs filter (for checking if an attr value has any hits). */
    private BoolQueryBuilder buildBaseBoolQuery(SearchParam param) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (param == null) return boolQuery;
        if (StringUtils.hasText(param.getKeyword())) {
            boolQuery.must(buildKeywordQuery(param.getKeyword()));
        }
        if (!CollectionUtils.isEmpty(param.getCatalog3Id())) {
            boolQuery.filter(QueryBuilders.termsQuery("catalogId", param.getCatalog3Id()));
        }
        if (!CollectionUtils.isEmpty(param.getBrandId())) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }
        if (param.getHasStock() != null && param.getHasStock() == 1) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", true));
        }
        if (StringUtils.hasText(param.getSkuPrice())) {
            String range = param.getSkuPrice().trim();
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            if (range.contains("_")) {
                String[] parts = range.split("_");
                if (parts.length >= 1 && !parts[0].isEmpty()) {
                    try {
                        rangeQuery.gte(Double.parseDouble(parts[0]));
                    } catch (NumberFormatException ignored) {
                    }
                }
                if (parts.length >= 2 && !parts[1].isEmpty()) {
                    try {
                        rangeQuery.lte(Double.parseDouble(parts[1]));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            boolQuery.filter(rangeQuery);
        }
        return boolQuery;
    }

    /** Return true if adding filter (attrId, value) still has at least one hit (exact or wildcard match). On error, return true to keep the value (fail-open). */
    private boolean hasHitsForAttrValue(BoolQueryBuilder baseBool, long attrId, String value) {
        if (!StringUtils.hasText(value)) return false;
        String v = value.trim();
        BoolQueryBuilder nestedBool = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("attrs.attrId", attrId));
        BoolQueryBuilder valueBool = QueryBuilders.boolQuery()
                .should(QueryBuilders.termQuery("attrs.attrValue.keyword", v))
                .should(QueryBuilders.wildcardQuery("attrs.attrValue.keyword", "*" + escapeWildcard(v) + "*"))
                .minimumShouldMatch(1);
        nestedBool.must(valueBool);
        NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBool, ScoreMode.None);
        BoolQueryBuilder fullQuery = QueryBuilders.boolQuery().must(baseBool).filter(nestedQuery);
        SearchSourceBuilder src = new SearchSourceBuilder().query(fullQuery).size(0);
        SearchRequest req = new SearchRequest(EsConstant.PRODUCT_INDEX).source(src);
        try {
            SearchResponse resp = esClient.search(req, GulimallElasticSearchConfiguration.COMMON_OPTIONS);
            return resp.getHits().getTotalHits().value > 0;
        } catch (Exception e) {
            // Fail-open: keep attribute value if count check fails (e.g. timeout, mapping)
            return true;
        }
    }
}
