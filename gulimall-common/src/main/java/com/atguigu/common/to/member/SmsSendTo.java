package com.atguigu.common.to.member;

import lombok.Data;

import java.io.Serializable;

@Data
public class SmsSendTo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String mobile;
}
