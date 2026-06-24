package com.atguigu.gulimall.search.service.impl;

import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfiguration;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.service.ProductSaveService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    private final RestHighLevelClient esClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProductSaveServiceImpl(RestHighLevelClient esRestClient) {
        this.esClient = esRestClient;
    }

    @Override
    public boolean productUp(List<SkuEsModel> skuEsModels) throws Exception {
        if (skuEsModels == null || skuEsModels.isEmpty()) {
            return true;
        }
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel model : skuEsModels) {
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(model.getSkuId().toString());
            Map<String, Object> source = toEsIndexSource(model);
            indexRequest.source(source, XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulk = esClient.bulk(bulkRequest, GulimallElasticSearchConfiguration.COMMON_OPTIONS);
        if (bulk.hasFailures()) {
            List<String> failedIds = Arrays.stream(bulk.getItems())
                    .filter(BulkItemResponse::isFailed)
                    .map(item -> item.getId() + ": " + item.getFailureMessage())
                    .collect(Collectors.toList());
            log.error("Product listing error, failed items: {}", failedIds);
            throw new RuntimeException("Product listing to ES failed: " + String.join("; ", failedIds));
        }
        return true;
    }

    @Override
    public boolean productDown(Long spuId) throws Exception {
        if (spuId == null) {
            return true;
        }
        DeleteByQueryRequest request = new DeleteByQueryRequest(EsConstant.PRODUCT_INDEX);
        request.setQuery(QueryBuilders.termQuery("spuId", spuId));
        esClient.deleteByQuery(request, GulimallElasticSearchConfiguration.COMMON_OPTIONS);
        return true;
    }

    /**
     * Build ES document: searchable SKU fields + brandId/catalogId + attrs(attrId, attrValue).
     * brandName/catalogName are indexed for keyword search; display names may still be refreshed from DB on read.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> toEsIndexSource(SkuEsModel model) {
        Map<String, Object> source = objectMapper.convertValue(model, Map.class);
        source.remove("brandImg");
        Object attrsObj = source.get("attrs");
        if (attrsObj instanceof List<?> attrsList) {
            List<Map<String, Object>> slimAttrs = new ArrayList<>();
            for (Object item : attrsList) {
                if (!(item instanceof Map<?, ?> attrMap)) {
                    continue;
                }
                Map<String, Object> slim = new LinkedHashMap<>();
                Object attrId = attrMap.get("attrId");
                Object attrValue = attrMap.get("attrValue");
                if (attrId != null) {
                    slim.put("attrId", attrId);
                }
                if (attrValue != null) {
                    slim.put("attrValue", attrValue);
                }
                if (!slim.isEmpty()) {
                    slimAttrs.add(slim);
                }
            }
            source.put("attrs", slimAttrs);
        }
        Object price = source.get("skuPrice");
        if (price instanceof BigDecimal) {
            source.put("skuPrice", ((BigDecimal) price).doubleValue());
        }
        return source;
    }
}
