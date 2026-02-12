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
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<PurchaseEntity>().getPageable(params);
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
        purchaseRepository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        purchaseRepository.deleteAllById((Iterable<Long>) ids);
    }

    @Override
    public PageUtils queryPageUnreceivePurchase(Map<String, Object> params) {
        Specification<PurchaseEntity> spec = (root, query, cb) ->
                cb.or(cb.equal(root.get("status"), 0), cb.equal(root.get("status"), 1));
        Pageable pageable = new Query<PurchaseEntity>().getPageable(params);
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
            getById(purchaseId);
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

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        purchaseRepository.save(purchaseEntity);
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
        boolean flag = true;
        for (PurchaseItemDoneVo item : doneVo.getItems()) {
            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()) {
                flag = false;
            }
            PurchaseDetailEntity detailEntity = purchaseDetailService.getById(item.getItemId());
            if (detailEntity != null) {
                detailEntity.setStatus(item.getStatus());
                purchaseDetailService.updateById(detailEntity);
            }
            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.FINISHED.getCode()) {
                PurchaseDetailEntity entity = purchaseDetailService.getById(item.getItemId());
                if (entity != null) {
                    wareSkuService.addStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum());
                }
            }
        }

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(doneVo.getId());
        purchaseEntity.setStatus(flag ? WareConstant.PurchaseStatusEnum.FINISHED.getCode() : WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        purchaseRepository.save(purchaseEntity);
    }
}
