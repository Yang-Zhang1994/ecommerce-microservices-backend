package com.atguigu.gulimall.product.service;

import java.util.Collection;

/**
 * Validates and performs cascade delete for SPU/SKU (product DB, warehouse, coupon, search).
 */
public interface ProductCascadeDeleteService {

    void deleteSkus(Collection<Long> skuIds);

    void deleteSpus(Collection<Long> spuIds);
}
