package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WareStockUnlockVo {
    /** When set, unlock rows from DB (preferred, idempotent). */
    private Long taskId;
    /** Legacy: unlock by lines only (does not update task detail status). */
    private List<LockedStockVo> locked = new ArrayList<>();
}
