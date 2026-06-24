package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.client.ProductApi;
import com.atguigu.gulimall.ware.service.SearchIndexNotifyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedHashSet;

@Service
public class SearchIndexNotifyServiceImpl implements SearchIndexNotifyService {

    private static final Logger log = LoggerFactory.getLogger(SearchIndexNotifyServiceImpl.class);

    @Autowired
    private ProductApi productApi;

    @Override
    public void notifyStockChanged(Long skuId) {
        if (skuId == null) {
            return;
        }
        try {
            R r = productApi.refreshSearchIndexBySkuId(skuId);
            boolean synced = r != null && r.getCode() != null && r.getCode() == 0
                    && Boolean.TRUE.equals(r.get("searchSynced"));
            if (synced) {
                log.debug("ES search index refreshed for skuId={}", skuId);
            } else {
                log.info("ES search index not refreshed for skuId={} (SPU may be off-sale or refresh skipped)", skuId);
            }
        } catch (Exception e) {
            log.warn("Failed to refresh ES search index for skuId={} (warehouse change kept): {}", skuId, e.getMessage());
            log.debug("ES refresh stack trace for skuId=" + skuId, e);
        }
    }

    @Override
    public void notifyStockChanged(Collection<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return;
        }
        LinkedHashSet<Long> distinct = new LinkedHashSet<>();
        for (Long skuId : skuIds) {
            if (skuId != null) {
                distinct.add(skuId);
            }
        }
        if (distinct.size() == 1) {
            notifyStockChanged(distinct.iterator().next());
            return;
        }
        try {
            R r = productApi.refreshSearchIndexBySkuIds(distinct.stream().toList());
            if (r == null || r.getCode() == null || r.getCode() != 0) {
                log.warn("Batch ES refresh returned non-ok for skuIds={}: {}", distinct, r);
            } else {
                log.debug("ES batch refresh: synced={}, requested={}", r.get("searchSyncedCount"), distinct.size());
            }
        } catch (Exception e) {
            log.warn("Batch ES refresh failed for skuIds={} (warehouse change kept): {}", distinct, e.getMessage());
            log.debug("ES batch refresh stack trace", e);
            for (Long skuId : distinct) {
                notifyStockChanged(skuId);
            }
        }
    }
}
