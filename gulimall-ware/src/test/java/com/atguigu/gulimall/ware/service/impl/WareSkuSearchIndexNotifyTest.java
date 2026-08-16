package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.enums.StockDetailLockStatus;
import com.atguigu.gulimall.ware.enums.WareOrderTaskStatusEnum;
import com.atguigu.gulimall.ware.repository.WareOrderTaskDetailRepository;
import com.atguigu.gulimall.ware.repository.WareOrderTaskRepository;
import com.atguigu.gulimall.ware.repository.WareSkuRepository;
import com.atguigu.gulimall.ware.service.SearchIndexNotifyService;
import com.atguigu.gulimall.ware.vo.LockedStockVo;
import com.atguigu.gulimall.ware.vo.WareStockUnlockVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Lock/unlock change available stock ({@code stock - stock_locked}); ES hasStock must refresh.
 * Without an active Spring transaction, refresh runs immediately (else branch of afterCommit helper).
 */
@ExtendWith(MockitoExtension.class)
class WareSkuSearchIndexNotifyTest {

    private static final long TASK_ID = 88L;

    @Mock
    private WareSkuRepository wareSkuRepository;

    @Mock
    private WareOrderTaskRepository wareOrderTaskRepository;

    @Mock
    private WareOrderTaskDetailRepository wareOrderTaskDetailRepository;

    @Mock
    private SearchIndexNotifyService searchIndexNotifyService;

    @InjectMocks
    private WareSkuServiceImpl wareSkuService;

    @Test
    void unlockByTaskId_notifiesSearchIndexForUnlockedSkus() {
        WareOrderTaskDetailEntity a = lockedDetail(201L, 1L, 1);
        WareOrderTaskDetailEntity b = lockedDetail(202L, 1L, 2);
        when(wareOrderTaskDetailRepository.findByTaskIdAndLockStatus(
                TASK_ID, StockDetailLockStatus.LOCKED.getCode()))
                .thenReturn(List.of(a, b));
        when(wareSkuRepository.unlockSkuStock(anyLong(), anyLong(), anyInt())).thenReturn(1);
        WareOrderTaskEntity task = new WareOrderTaskEntity();
        task.setId(TASK_ID);
        when(wareOrderTaskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

        wareSkuService.unlockByTaskId(TASK_ID);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<Long>> skuIds = ArgumentCaptor.forClass(Collection.class);
        verify(searchIndexNotifyService).notifyStockChanged(skuIds.capture());
        assertThat(skuIds.getValue()).containsExactlyInAnyOrder(201L, 202L);
        assertThat(task.getTaskStatus()).isEqualTo(WareOrderTaskStatusEnum.STOCK_RELEASED.getCode());
    }

    @Test
    void unlockByTaskId_skipsSearchNotifyWhenNothingLocked() {
        when(wareOrderTaskDetailRepository.findByTaskIdAndLockStatus(
                TASK_ID, StockDetailLockStatus.LOCKED.getCode()))
                .thenReturn(List.of());

        wareSkuService.unlockByTaskId(TASK_ID);

        verify(searchIndexNotifyService, never()).notifyStockChanged(anyLong());
        verify(searchIndexNotifyService, never()).notifyStockChanged(anyCollection());
    }

    @Test
    void unlockOrderStock_legacyLines_notifiesSearchIndex() {
        WareStockUnlockVo vo = new WareStockUnlockVo();
        LockedStockVo line = new LockedStockVo();
        line.setSkuId(301L);
        line.setWareId(1L);
        line.setCount(3);
        vo.setLocked(List.of(line));
        when(wareSkuRepository.unlockSkuStock(301L, 1L, 3)).thenReturn(1);

        wareSkuService.unlockOrderStock(vo);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<Long>> skuIds = ArgumentCaptor.forClass(Collection.class);
        verify(searchIndexNotifyService).notifyStockChanged(skuIds.capture());
        assertThat(skuIds.getValue()).containsExactly(301L);
        verify(wareOrderTaskDetailRepository, never()).findByTaskIdAndLockStatus(any(), any());
    }

    private WareOrderTaskDetailEntity lockedDetail(Long skuId, Long wareId, int num) {
        WareOrderTaskDetailEntity detail = new WareOrderTaskDetailEntity();
        detail.setTaskId(TASK_ID);
        detail.setSkuId(skuId);
        detail.setWareId(wareId);
        detail.setSkuNum(num);
        detail.setLockStatus(StockDetailLockStatus.LOCKED.getCode());
        return detail;
    }
}
