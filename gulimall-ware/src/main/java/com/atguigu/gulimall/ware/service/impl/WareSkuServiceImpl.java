package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.to.ware.SkuHasStockVo;
import com.atguigu.common.utils.R;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.ware.config.StockRabbitMqConfig;
import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.client.ProductApi;
import com.atguigu.gulimall.ware.enums.StockDetailLockStatus;
import com.atguigu.gulimall.ware.enums.WareOrderTaskStatusEnum;
import com.atguigu.gulimall.ware.repository.WareOrderTaskDetailRepository;
import com.atguigu.gulimall.ware.repository.WareOrderTaskRepository;
import com.atguigu.gulimall.ware.repository.WareSkuRepository;
import com.atguigu.gulimall.ware.service.SearchIndexNotifyService;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.LockedStockVo;
import com.atguigu.gulimall.ware.vo.OrderItemLockVo;
import com.atguigu.gulimall.ware.vo.OrderLockStockResultVo;
import com.atguigu.gulimall.ware.vo.OrderWareLockVo;
import com.atguigu.gulimall.ware.vo.StockDelayMessage;
import com.atguigu.gulimall.ware.vo.WareStockUnlockVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service("wareSkuService")
public class WareSkuServiceImpl implements WareSkuService {

    @Autowired
    private WareSkuRepository wareSkuRepository;
    @Autowired
    private ProductApi productApi;
    @Autowired
    private SearchIndexNotifyService searchIndexNotifyService;
    @Autowired
    private WareOrderTaskRepository wareOrderTaskRepository;
    @Autowired
    private WareOrderTaskDetailRepository wareOrderTaskDetailRepository;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
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
        Pageable pageable = new Query<WareSkuEntity>().getPageable(params, Sort.by("id").ascending());
        Page<WareSkuEntity> page = wareSkuRepository.findAll(spec, pageable);
        return new PageUtils(page);
    }

    @Override
    public WareSkuEntity getById(Long id) {
        return wareSkuRepository.findById(id).orElse(null);
    }

    @Override
    public void save(WareSkuEntity entity) {
        enrichFromProduct(entity);
        wareSkuRepository.save(entity);
    }

    @Override
    public void updateById(WareSkuEntity entity) {
        enrichFromProduct(entity);
        wareSkuRepository.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> syncFromProduct() {
        List<WareSkuEntity> all = wareSkuRepository.findAll();
        int updated = 0;
        int failed = 0;
        for (WareSkuEntity entity : all) {
            boolean changed = false;
            if (entity.getStockLocked() == null) {
                entity.setStockLocked(0);
                changed = true;
            }
            String latestName = resolveSkuName(entity.getSkuId());
            if (latestName != null && !Objects.equals(latestName, entity.getSkuName())) {
                entity.setSkuName(latestName);
                changed = true;
            }
            if (changed) {
                wareSkuRepository.save(entity);
                updated++;
            }
            if (entity.getSkuId() != null && latestName == null) {
                failed++;
            }
        }
        Map<String, Object> out = new HashMap<>();
        out.put("total", all.size());
        out.put("updated", updated);
        out.put("failed", failed);
        return out;
    }

    private void enrichFromProduct(WareSkuEntity entity) {
        if (entity == null) {
            return;
        }
        if (entity.getStockLocked() == null) {
            entity.setStockLocked(0);
        }
        if (entity.getSkuId() != null && !StringUtils.hasText(entity.getSkuName())) {
            String name = resolveSkuName(entity.getSkuId());
            if (name != null) {
                entity.setSkuName(name);
            }
        }
    }

    private String resolveSkuName(Long skuId) {
        if (skuId == null) {
            return null;
        }
        try {
            R info = productApi.info(skuId);
            Object skuInfoObj = info.get("skuInfo");
            if (skuInfoObj instanceof Map<?, ?> skuInfo) {
                Object skuName = skuInfo.get("skuName");
                if (skuName != null && StringUtils.hasText(String.valueOf(skuName))) {
                    return String.valueOf(skuName).trim();
                }
                Object skuTitle = skuInfo.get("skuTitle");
                if (skuTitle != null && StringUtils.hasText(String.valueOf(skuTitle))) {
                    return String.valueOf(skuTitle).trim();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<Long> idList = ids.stream()
                .map(one -> {
                    if (one instanceof Number n) return n.longValue();
                    try {
                        return Long.parseLong(String.valueOf(one));
                    } catch (Exception ignore) {
                        return null;
                    }
                })
                .filter(v -> v != null)
                .toList();
        if (!idList.isEmpty()) {
            wareSkuRepository.deleteAllById(idList);
        }
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
                Object skuInfoObj = info.get("skuInfo");
                if (skuInfoObj instanceof Map<?, ?> skuInfo) {
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
        if (skuNum != null && skuNum > 0) {
            scheduleSearchIndexRefreshAfterCommit(skuId);
        }
    }

    /** After WMS commit, product re-reads stock and updates ES hasStock. */
    private void scheduleSearchIndexRefreshAfterCommit(Long skuId) {
        if (skuId == null) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    searchIndexNotifyService.notifyStockChanged(skuId);
                }
            });
        } else {
            searchIndexNotifyService.notifyStockChanged(skuId);
        }
    }

    @Override
    public List<SkuHasStockVo> hasStockBySkuIds(List<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return List.of();
        }
        // 按图片逻辑：逐个 skuId 查询当前 sku 的总可用库存（SUM(stock - stock_locked)），再封装为 SkuHasStockVo
        return skuIds.stream().map(skuId -> {
            SkuHasStockVo vo = new SkuHasStockVo();
            vo.setSkuId(skuId);
            // 查询当前 sku 的总库存量：SELECT SUM(stock - stock_locked) FROM wms_ware_sku WHERE sku_id = ?
            Long count = wareSkuRepository.getSkuStock(skuId);
            vo.setHasStock(count != null && count > 0);
            return vo;
        }).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderLockStockResultVo lockOrderStock(OrderWareLockVo lockVo) {
        OrderLockStockResultVo out = new OrderLockStockResultVo();
        if (lockVo == null || lockVo.getLocks() == null || lockVo.getLocks().isEmpty()) {
            return out;
        }
        if (!StringUtils.hasText(lockVo.getOrderSn())) {
            throw new IllegalStateException("orderSn is required for stock work order");
        }

        WareOrderTaskEntity task = new WareOrderTaskEntity();
        task.setOrderSn(lockVo.getOrderSn().trim());
        task.setCreateTime(new Date());
        task.setTaskStatus(WareOrderTaskStatusEnum.CREATED.getCode());
        wareOrderTaskRepository.save(task);
        wareOrderTaskRepository.flush();

        List<LockedStockVo> locked = new ArrayList<>();
        List<WareOrderTaskDetailEntity> details = new ArrayList<>();
        for (OrderItemLockVo item : lockVo.getLocks()) {
            Long skuId = item.getSkuId();
            int need = item.getCount() == null ? 0 : item.getCount();
            if (skuId == null || need <= 0) continue;
            List<WareSkuEntity> candidates = wareSkuRepository.findBySkuId(skuId);
            boolean success = false;
            for (WareSkuEntity ws : candidates) {
                if (ws.getWareId() == null) continue;
                int updated = wareSkuRepository.lockSkuStock(skuId, ws.getWareId(), need);
                if (updated > 0) {
                    LockedStockVo vo = new LockedStockVo();
                    vo.setSkuId(skuId);
                    vo.setWareId(ws.getWareId());
                    vo.setCount(need);
                    locked.add(vo);

                    WareOrderTaskDetailEntity detail = new WareOrderTaskDetailEntity();
                    detail.setTaskId(task.getId());
                    detail.setSkuId(skuId);
                    detail.setSkuName(ws.getSkuName() != null ? ws.getSkuName() : "");
                    detail.setSkuNum(need);
                    detail.setWareId(ws.getWareId());
                    detail.setLockStatus(StockDetailLockStatus.LOCKED.getCode());
                    details.add(detail);
                    success = true;
                    break;
                }
            }
            if (!success) {
                throw new IllegalStateException("No stock available for skuId=" + skuId);
            }
        }
        if (!details.isEmpty()) {
            wareOrderTaskDetailRepository.saveAll(details);
        }

        out.setTaskId(task.getId());
        out.setLocked(locked);

        Long taskIdForMq = task.getId();
        String orderSnForMq = lockVo.getOrderSn().trim();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    StockDelayMessage msg = new StockDelayMessage();
                    msg.setTaskId(taskIdForMq);
                    msg.setOrderSn(orderSnForMq);
                    String json = objectMapper.writeValueAsString(msg);
                    rabbitTemplate.convertAndSend(
                            StockRabbitMqConfig.STOCK_EVENT_EXCHANGE,
                            StockRabbitMqConfig.ROUTING_KEY_STOCK_LOCKED,
                            json
                    );
                } catch (Exception ex) {
                    org.slf4j.LoggerFactory.getLogger(WareSkuServiceImpl.class)
                            .warn("stock-delay message send failed taskId={} orderSn={}", taskIdForMq, orderSnForMq, ex);
                }
            }
        });

        return out;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlockOrderStock(WareStockUnlockVo vo) {
        if (vo == null) {
            return;
        }
        if (vo.getTaskId() != null) {
            unlockByTaskId(vo.getTaskId());
            return;
        }
        if (vo.getLocked() == null || vo.getLocked().isEmpty()) {
            return;
        }
        for (LockedStockVo line : vo.getLocked()) {
            if (line == null || line.getSkuId() == null || line.getWareId() == null) continue;
            int num = line.getCount() == null ? 0 : line.getCount();
            if (num <= 0) continue;
            wareSkuRepository.unlockSkuStock(line.getSkuId(), line.getWareId(), num);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlockByTaskId(Long taskId) {
        if (taskId == null) {
            return;
        }
        List<WareOrderTaskDetailEntity> lines = wareOrderTaskDetailRepository.findByTaskIdAndLockStatus(
                taskId, StockDetailLockStatus.LOCKED.getCode());
        if (lines.isEmpty()) {
            return;
        }
        for (WareOrderTaskDetailEntity d : lines) {
            if (d.getSkuId() == null || d.getWareId() == null || d.getSkuNum() == null || d.getSkuNum() <= 0) {
                continue;
            }
            wareSkuRepository.unlockSkuStock(d.getSkuId(), d.getWareId(), d.getSkuNum());
            d.setLockStatus(StockDetailLockStatus.UNLOCKED.getCode());
        }
        wareOrderTaskDetailRepository.saveAll(lines);
        wareOrderTaskRepository.findById(taskId).ifPresent(t -> {
            t.setTaskStatus(WareOrderTaskStatusEnum.STOCK_RELEASED.getCode());
            wareOrderTaskRepository.save(t);
        });
    }

    @Override
    public List<Long> findSkuIdsWithLockedStock(List<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return List.of();
        }
        return wareSkuRepository.findSkuIdsWithLockedStock(skuIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBySkuIds(List<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return;
        }
        wareSkuRepository.deleteBySkuIdIn(skuIds);
    }
}
