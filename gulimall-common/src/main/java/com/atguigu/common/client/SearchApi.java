package com.atguigu.common.client;

import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

/**
 * HTTP Interface for gulimall-search (商品上架/下架同步到 ES).
 */
@HttpExchange
public interface SearchApi {

    @PostExchange("/search/product/up")
    R productUp(@RequestBody List<SkuEsModel> skuEsModels);

    /** 商品下架：从 ES 删除该 SPU 下所有 SKU 文档 */
    @PostExchange("/search/product/down/{spuId}")
    R productDown(@PathVariable("spuId") Long spuId);
}
