package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.utils.R;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.client.ProductApi;
import com.atguigu.gulimall.ware.repository.WareSkuRepository;
import com.atguigu.gulimall.ware.service.WareSkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service("wareSkuService")
public class WareSkuServiceImpl implements WareSkuService {

    @Autowired
    private WareSkuRepository wareSkuRepository;
    @Autowired
    private ProductApi productApi;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String skuId = (String) params.get("skuId");
        String wareId = (String) params.get("wareId");
        Specification<WareSkuEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (skuId != null && !skuId.isEmpty()) {
                try {
                    predicates.add(cb.equal(root.get("skuId"), Long.parseLong(skuId)));
                } catch (NumberFormatException ignored) {}
            }
            if (wareId != null && !wareId.isEmpty()) {
                try {
                    predicates.add(cb.equal(root.get("wareId"), Long.parseLong(wareId)));
                } catch (NumberFormatException ignored) {}
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
        Pageable pageable = new Query<WareSkuEntity>().getPageable(params);
        Page<WareSkuEntity> page = wareSkuRepository.findAll(spec, pageable);
        return new PageUtils(page);
    }

    @Override
    public WareSkuEntity getById(Long id) {
        return wareSkuRepository.findById(id).orElse(null);
    }

    @Override
    public void save(WareSkuEntity entity) {
        wareSkuRepository.save(entity);
    }

    @Override
    public void updateById(WareSkuEntity entity) {
        wareSkuRepository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        wareSkuRepository.deleteAllById((Iterable<Long>) ids);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        List<WareSkuEntity> list = wareSkuRepository.findBySkuIdAndWareId(skuId, wareId);
        if (list == null || list.isEmpty()) {
            WareSkuEntity entity = new WareSkuEntity();
            entity.setSkuId(skuId);
            entity.setWareId(wareId);
            entity.setStock(skuNum);
            entity.setStockLocked(0);
            try {
                R info = productApi.info(skuId);
                Map<String, Object> skuInfo = (Map<String, Object>) info.get("skuInfo");
                if (skuInfo != null) {
                    String skuName = (String) skuInfo.get("skuName");
                    entity.setSkuName(skuName);
                }
            } catch (Exception ignored) {}
            wareSkuRepository.save(entity);
        } else {
            WareSkuEntity entity = list.get(0);
            entity.setStock((entity.getStock() != null ? entity.getStock() : 0) + skuNum);
            wareSkuRepository.save(entity);
        }
    }
}
