package com.atguigu.gulimall.ware.service;

import java.util.Collection;

/**
 * Notify product service to refresh Elasticsearch after warehouse stock changes.
 * Failures are logged only; warehouse transactions must not depend on ES success.
 */
public interface SearchIndexNotifyService {

    void notifyStockChanged(Long skuId);

    void notifyStockChanged(Collection<Long> skuIds);
}
