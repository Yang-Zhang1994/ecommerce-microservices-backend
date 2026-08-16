package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.enums.StockDetailLockStatus;
import com.atguigu.gulimall.ware.enums.WareOrderTaskStatusEnum;
import com.atguigu.gulimall.ware.repository.WareOrderTaskDetailRepository;
import com.atguigu.gulimall.ware.repository.WareOrderTaskRepository;
import com.atguigu.gulimall.ware.repository.WareSkuRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Payment success ({@code order.finish.ware}) turns locked stock into a real deduction, and a
 * redelivered message must not deduct twice.
 */
@ExtendWith(MockitoExtension.class)
class WareSkuStockDeductTest {

    private static final long TASK_ID = 77L;

    @Mock
    private WareSkuRepository wareSkuRepository;

    @Mock
    private WareOrderTaskRepository wareOrderTaskRepository;

    @Mock
    private WareOrderTaskDetailRepository wareOrderTaskDetailRepository;

    @InjectMocks
    private WareSkuServiceImpl wareSkuService;

    @Test
    void deductByTaskId_deductsLockedLinesAndMarksTaskDeducted() {
        WareOrderTaskDetailEntity first = detail(101L, 1L, 2, StockDetailLockStatus.LOCKED);
        WareOrderTaskDetailEntity second = detail(102L, 1L, 3, StockDetailLockStatus.LOCKED);
        when(wareOrderTaskDetailRepository.findByTaskId(TASK_ID)).thenReturn(List.of(first, second));
        when(wareSkuRepository.deductSkuStock(anyLong(), anyLong(), anyInt())).thenReturn(1);
        WareOrderTaskEntity task = new WareOrderTaskEntity();
        task.setId(TASK_ID);
        task.setTaskStatus(WareOrderTaskStatusEnum.CREATED.getCode());
        when(wareOrderTaskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

        int deducted = wareSkuService.deductByTaskId(TASK_ID);

        assertThat(deducted).isEqualTo(2);
        verify(wareSkuRepository).deductSkuStock(101L, 1L, 2);
        verify(wareSkuRepository).deductSkuStock(102L, 1L, 3);
        assertThat(first.getLockStatus()).isEqualTo(StockDetailLockStatus.DEDUCTED.getCode());
        assertThat(second.getLockStatus()).isEqualTo(StockDetailLockStatus.DEDUCTED.getCode());

        ArgumentCaptor<WareOrderTaskEntity> saved = ArgumentCaptor.forClass(WareOrderTaskEntity.class);
        verify(wareOrderTaskRepository).save(saved.capture());
        assertThat(saved.getValue().getTaskStatus())
                .isEqualTo(WareOrderTaskStatusEnum.STOCK_DEDUCTED.getCode());
    }

    @Test
    void deductByTaskId_isNoOpWhenMessageIsRedelivered() {
        WareOrderTaskDetailEntity alreadyDone = detail(101L, 1L, 2, StockDetailLockStatus.DEDUCTED);
        when(wareOrderTaskDetailRepository.findByTaskId(TASK_ID)).thenReturn(List.of(alreadyDone));

        int deducted = wareSkuService.deductByTaskId(TASK_ID);

        assertThat(deducted).isZero();
        verify(wareSkuRepository, never()).deductSkuStock(anyLong(), anyLong(), anyInt());
        verify(wareOrderTaskRepository, never()).save(any());
    }

    @Test
    void deductByTaskId_keepsLineLockedWhenDatabaseGuardRejectsDeduction() {
        WareOrderTaskDetailEntity line = detail(101L, 1L, 2, StockDetailLockStatus.LOCKED);
        when(wareOrderTaskDetailRepository.findByTaskId(TASK_ID)).thenReturn(List.of(line));
        when(wareSkuRepository.deductSkuStock(101L, 1L, 2)).thenReturn(0);

        int deducted = wareSkuService.deductByTaskId(TASK_ID);

        assertThat(deducted).isZero();
        assertThat(line.getLockStatus()).isEqualTo(StockDetailLockStatus.LOCKED.getCode());
        verify(wareOrderTaskDetailRepository, never()).save(any());
        verify(wareOrderTaskRepository, never()).save(any());
    }

    private WareOrderTaskDetailEntity detail(Long skuId, Long wareId, int num, StockDetailLockStatus status) {
        WareOrderTaskDetailEntity detail = new WareOrderTaskDetailEntity();
        detail.setTaskId(TASK_ID);
        detail.setSkuId(skuId);
        detail.setWareId(wareId);
        detail.setSkuNum(num);
        detail.setLockStatus(status.getCode());
        return detail;
    }
}
