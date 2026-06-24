package com.atguigu.gulimall.ware.enums;

/**
 * wms_ware_order_task.task_status — stock work-order lifecycle (high level).
 * <p>
 * Detail line state remains in {@link StockDetailLockStatus} on {@code wms_ware_order_task_detail}.
 */
public enum WareOrderTaskStatusEnum {
    /** Work order created; inventory locked for the order (awaiting pay / ship / timeout release). */
    CREATED(1),
    /** Locked stock was released (timeout, cancel, or manual unlock). */
    STOCK_RELEASED(2),
    /** Payment (or business) confirmed; stock deducted from real inventory (not implemented in ware yet). */
    STOCK_DEDUCTED(3);

    private final int code;

    WareOrderTaskStatusEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
