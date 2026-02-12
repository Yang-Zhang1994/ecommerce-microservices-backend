package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.repository.PurchaseDetailRepository;
import com.atguigu.gulimall.ware.service.PurchaseDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl implements PurchaseDetailService {

    @Autowired
    private PurchaseDetailRepository repository;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String) params.get("key");
        String status = (String) params.get("status");
        String wareId = (String) params.get("wareId");
        Specification<PurchaseDetailEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();
            if (key != null && !key.isEmpty()) {
                try {
                    Long keyLong = Long.parseLong(key);
                    predicates.add(cb.or(
                            cb.equal(root.get("purchaseId"), keyLong),
                            cb.equal(root.get("skuId"), keyLong)
                    ));
                } catch (NumberFormatException ignored) {}
            }
            if (status != null && !status.isEmpty()) {
                try {
                    predicates.add(cb.equal(root.get("status"), Integer.parseInt(status)));
                } catch (NumberFormatException ignored) {}
            }
            if (wareId != null && !wareId.isEmpty()) {
                try {
                    predicates.add(cb.equal(root.get("wareId"), Long.parseLong(wareId)));
                } catch (NumberFormatException ignored) {}
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
        Pageable pageable = new Query<PurchaseDetailEntity>().getPageable(params);
        Page<PurchaseDetailEntity> page = repository.findAll(spec, pageable);
        return new PageUtils(page);
    }

    @Override
    public PurchaseDetailEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void save(PurchaseDetailEntity entity) {
        repository.save(entity);
    }

    @Override
    public void updateById(PurchaseDetailEntity entity) {
        repository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        repository.deleteAllById((Iterable<Long>) ids);
    }

    @Override
    public List<PurchaseDetailEntity> listByIds(Collection<Long> ids) {
        return repository.findAllById(ids);
    }

    @Override
    public void updateBatchById(List<PurchaseDetailEntity> list) {
        repository.saveAll(list);
    }

    @Override
    public List<PurchaseDetailEntity> listDetailByPurchaseId(Long purchaseId) {
        return repository.findByPurchaseId(purchaseId);
    }
}
