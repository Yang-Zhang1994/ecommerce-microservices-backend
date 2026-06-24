package com.atguigu.gulimall.search.service.impl;

import com.atguigu.gulimall.search.config.GulimallElasticSearchConfiguration;
import com.atguigu.gulimall.search.constant.EsConstant;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Ensures the product index exists with nested {@code attrs} and keyword subfields required by search aggregations.
 */
@Slf4j
@Component
public class ProductIndexInitializer implements ApplicationRunner {

    private static final String PRODUCT_MAPPING = """
            {
              "properties": {
                "skuId": { "type": "long" },
                "spuId": { "type": "long" },
                "spuName": { "type": "text", "fields": { "keyword": { "type": "keyword" } } },
                "skuTitle": { "type": "text", "fields": { "keyword": { "type": "keyword" } } },
                "skuPrice": { "type": "double" },
                "skuImg": { "type": "keyword", "ignore_above": 2048 },
                "saleCount": { "type": "long" },
                "hasStock": { "type": "boolean" },
                "hotScore": { "type": "long" },
                "brandId": { "type": "long" },
                "catalogId": { "type": "long" },
                "brandName": { "type": "text", "fields": { "keyword": { "type": "keyword" } } },
                "catalogName": { "type": "text", "fields": { "keyword": { "type": "keyword" } } },
                "attrs": {
                  "type": "nested",
                  "properties": {
                    "attrId": { "type": "long" },
                    "attrValue": {
                      "type": "text",
                      "fields": { "keyword": { "type": "keyword" } }
                    }
                  }
                }
              }
            }
            """;

    private final RestHighLevelClient esClient;

    public ProductIndexInitializer(RestHighLevelClient esRestClient) {
        this.esClient = esRestClient;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ensureProductIndex();
    }

    void ensureProductIndex() throws Exception {
        GetIndexRequest getIndexRequest = new GetIndexRequest(EsConstant.PRODUCT_INDEX);
        boolean exists = esClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        if (exists) {
            return;
        }
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(EsConstant.PRODUCT_INDEX);
        createIndexRequest.mapping(PRODUCT_MAPPING, XContentType.JSON);
        esClient.indices().create(createIndexRequest, GulimallElasticSearchConfiguration.COMMON_OPTIONS);
        log.info("Created Elasticsearch index '{}' with nested attrs mapping", EsConstant.PRODUCT_INDEX);
    }
}
