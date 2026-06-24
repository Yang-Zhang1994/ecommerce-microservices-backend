package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.constant.WareConstant;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;
import com.atguigu.gulimall.ware.repository.PurchaseRepository;
import com.atguigu.gulimall.ware.service.PurchaseDetailService;
import com.atguigu.gulimall.ware.service.PurchaseService;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.MergeVo;
import com.atguigu.gulimall.ware.vo.PurchaseDoneVo;
import com.atguigu.gulimall.ware.vo.PurchaseItemDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("purchaseService")
public class PurchaseServiceImpl implements PurchaseService {

    @Autowired
    private PurchaseRepository purchaseRepository;
    @Autowired
    private PurchaseDetailService purchaseDetailService;
    @Autowired
    private WareSkuService wareSkuService;

    @Override
    @Transactional(readOnly = true)
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<PurchaseEntity>().getPageable(params, Sort.by("id").ascending());
        Page<PurchaseEntity> page = purchaseRepository.findAll(pageable);
        return new PageUtils(page);
    }

    @Override
    public PurchaseEntity getById(Long id) {
        return purchaseRepository.findById(id).orElse(null);
    }

    @Override
    public void save(PurchaseEntity entity) {
        purchaseRepository.save(entity);
    }

    @Override
    public void updateById(PurchaseEntity entity) {
        if (entity == null || entity.getId() == null) {
            return;
        }
        PurchaseEntity existing = purchaseRepository.findById(entity.getId()).orElse(null);
        if (existing == null) {
            return;
        }
        if (entity.getAssigneeId() != null) {
            existing.setAssigneeId(entity.getAssigneeId());
        }
        if (entity.getAssigneeName() != null) {
            existing.setAssigneeName(entity.getAssigneeName());
        }
        if (entity.getPhone() != null) {
            existing.setPhone(entity.getPhone());
        }
        if (entity.getPriority() != null) {
            existing.setPriority(entity.getPriority());
        }
        if (entity.getStatus() != null) {
            existing.setStatus(entity.getStatus());
        }
        if (entity.getWareId() != null) {
            existing.setWareId(entity.getWareId());
        }
        if (entity.getAmount() != null) {
            existing.setAmount(entity.getAmount());
        }
        if (entity.getCreateTime() != null) {
            existing.setCreateTime(entity.getCreateTime());
        }
        if (entity.getUpdateTime() != null) {
            existing.setUpdateTime(entity.getUpdateTime());
        }
        purchaseRepository.save(existing);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        purchaseRepository.deleteAllById((Iterable<Long>) ids);
    }

    @Override
    @Transactional(readOnly = true)
    public PageUtils queryPageUnreceivePurchase(Map<String, Object> params) {
        Specification<PurchaseEntity> spec = (root, query, cb) ->
                cb.or(cb.equal(root.get("status"), 0), cb.equal(root.get("status"), 1));
        Pageable pageable = new Query<PurchaseEntity>().getPageable(params, Sort.by("id").ascending());
        Page<PurchaseEntity> page = purchaseRepository.findAll(spec, pageable);
        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if (purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchaseRepository.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        } else {
            PurchaseEntity existing = getById(purchaseId);
            if (existing != null) {
                existing.setUpdateTime(new Date());
                purchaseRepository.save(existing);
            }
        }

        if (mergeVo.getItems() != null && mergeVo.getItems().length > 0) {
            List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailService.listByIds(
                    Arrays.stream(mergeVo.getItems()).toList()
            );
            List<PurchaseDetailEntity> invalidDetails = purchaseDetailEntities.stream().filter(i ->
                    i.getStatus() != WareConstant.PurchaseDetailStatusEnum.CREATED.getCode() &&
                            i.getStatus() != WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode()
            ).toList();
            if (!invalidDetails.isEmpty()) {
                throw new IllegalArgumentException("Some purchase details are not in status of created or assigned: " +
                        invalidDetails.stream().map(i -> i.getId().toString()).collect(Collectors.joining(", ")));
            }
        }

        Long[] items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> list = Arrays.stream(items).map(i -> {
            PurchaseDetailEntity detail = purchaseDetailService.getById(i);
            if (detail != null) {
                detail.setPurchaseId(finalPurchaseId);
                detail.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            }
            return detail;
        }).filter(e -> e != null).toList();

        purchaseDetailService.updateBatchById(list);
    }

    @Override
    public void received(List<Long> ids) {
        List<PurchaseEntity> purchaseEntities = ids.stream().map(this::getById).filter(i ->
                i != null && (i.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() ||
                        i.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode())
        ).map(i -> {
            i.setStatus(WareConstant.PurchaseStatusEnum.RECEIVED.getCode());
            i.setUpdateTime(new Date());
            return i;
        }).toList();

        purchaseRepository.saveAll(purchaseEntities);

        purchaseEntities.forEach(i -> {
            List<PurchaseDetailEntity> detailEntities = purchaseDetailService.listDetailByPurchaseId(i.getId());
            detailEntities.forEach(item -> {
                item.setStatus(WareConstant.PurchaseDetailStatusEnum.PURCHASING.getCode());
            });
            purchaseDetailService.updateBatchById(detailEntities);
        });
    }

    @Transactional
    @Override
    public void done(PurchaseDoneVo doneVo) {
        if (doneVo == null || doneVo.getId() == null) {
            throw new IllegalArgumentException("Purchase id is required");
        }
        if (doneVo.getItems() == null || doneVo.getItems().isEmpty()) {
            throw new IllegalArgumentException("At least one purchase detail item is required");
        }
        Long purchaseId = doneVo.getId();
        PurchaseEntity purchaseEntity = getById(purchaseId);
        if (purchaseEntity == null) {
            throw new IllegalArgumentException("Purchase order not found: id=" + purchaseId);
        }

        for (PurchaseItemDoneVo item : doneVo.getItems()) {
            if (item.getStatus() == null) {
                throw new IllegalArgumentException("Each item must include status");
            }
            PurchaseDetailEntity detail = resolvePurchaseDetail(purchaseId, item);
            detail.setStatus(item.getStatus());
            purchaseDetailService.updateById(detail);
            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.FINISHED.getCode()) {
                wareSkuService.addStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum());
            }
        }

        List<PurchaseDetailEntity> allDetails = purchaseDetailService.listDetailByPurchaseId(purchaseId);
        if (allDetails.isEmpty()) {
            throw new IllegalArgumentException("Purchase order has no detail lines: id=" + purchaseId);
        }
        boolean anyError = allDetails.stream()
                .anyMatch(d -> d.getStatus() != null
                        && d.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode());
        boolean allTerminal = allDetails.stream().allMatch(d -> {
            if (d.getStatus() == null) {
                return false;
            }
            int s = d.getStatus();
            return s == WareConstant.PurchaseDetailStatusEnum.FINISHED.getCode()
                    || s == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode();
        });
        if (allTerminal) {
            purchaseEntity.setStatus(anyError
                    ? WareConstant.PurchaseStatusEnum.HASERROR.getCode()
                    : WareConstant.PurchaseStatusEnum.FINISHED.getCode());
        } else {
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.RECEIVED.getCode());
        }
        purchaseEntity.setUpdateTime(new Date());
        purchaseRepository.save(purchaseEntity);
    }

    /**
     * itemId = wms_purchase_detail.id (NOT sku id). When skuId is sent instead, resolve by purchaseId + skuId.
     */
    private PurchaseDetailEntity resolvePurchaseDetail(Long purchaseId, PurchaseItemDoneVo item) {
        if (item.getItemId() != null) {
            PurchaseDetailEntity byId = purchaseDetailService.getById(item.getItemId());
            if (byId != null) {
                if (!purchaseId.equals(byId.getPurchaseId())) {
                    throw new IllegalArgumentException(
                            "Detail id=" + item.getItemId() + " belongs to purchase "
                                    + byId.getPurchaseId() + ", not purchase " + purchaseId
                                    + ". Use the correct detail id or pass skuId instead.");
                }
                return byId;
            }
        }
        if (item.getSkuId() != null) {
            PurchaseDetailEntity bySku = purchaseDetailService.getByPurchaseIdAndSkuId(purchaseId, item.getSkuId());
            if (bySku != null) {
                return bySku;
            }
            throw new IllegalArgumentException(
                    "No purchase detail for purchaseId=" + purchaseId + " skuId=" + item.getSkuId());
        }
        throw new IllegalArgumentException(
                "Purchase detail not found. itemId is wms_purchase_detail.id (not sku id). "
                        + "Query GET /ware/purchasedetail/list?key=" + purchaseId
                        + " or pass skuId with purchase = purchased product id.");
    }
}
