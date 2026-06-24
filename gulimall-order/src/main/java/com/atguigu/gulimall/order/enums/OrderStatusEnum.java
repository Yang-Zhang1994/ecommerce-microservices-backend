package com.atguigu.gulimall.order.enums;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Order lifecycle status codes in oms_order.status.
 */
public enum OrderStatusEnum {
    CREATE_NEW(0, "Pending payment"),
    PAYED(1, "Paid"),
    SHIPPED(2, "Shipped"),
    COMPLETED(3, "Completed"),
    CLOSED(4, "Closed"),
    INVALID(5, "Invalid");

    private final int code;
    private final String desc;

    OrderStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static OrderStatusEnum fromCode(Integer code) {
        if (code == null) return null;
        return Arrays.stream(values())
                .filter(v -> v.code == code)
                .findFirst()
                .orElse(null);
    }

    public static boolean isFinalStatus(Integer code) {
        OrderStatusEnum s = fromCode(code);
        return s == COMPLETED || s == CLOSED || s == INVALID;
    }

    public static Set<Integer> allCodes() {
        return Arrays.stream(values()).map(OrderStatusEnum::getCode).collect(Collectors.toSet());
    }
}

