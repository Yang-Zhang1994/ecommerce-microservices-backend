package com.atguigu.gulimall.search.service;

import com.atguigu.common.to.es.SkuEsModel;

import java.util.List;

/**
 * Product listing: write SkuEsModel documents into Elasticsearch.
 * @return true if all succeed; on failure or exception an error is thrown and handled by the controller.
 */
public interface ProductSaveService {

    boolean productUp(List<SkuEsModel> skuEsModels) throws Exception;

    /**
     * Product off-shelf: remove all SKU documents of the given SPU from Elasticsearch.
     * @return true if the operation completed successfully
     */
    boolean productDown(Long spuId) throws Exception;
}
