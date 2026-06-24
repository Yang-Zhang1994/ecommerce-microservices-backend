package com.atguigu.gulimall.ware.enums;

/**
 * wms_ware_order_task_detail.lock_status
 */
public enum StockDetailLockStatus {
    LOCKED(1),
    UNLOCKED(2),
    DEDUCTED(3);

    private final int code;

    StockDetailLockStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
