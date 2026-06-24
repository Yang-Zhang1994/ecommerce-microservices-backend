package com.atguigu.common.to;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/** Seckill session row exposed to gulimall-seckill via HTTP (coupon owns the DB). */
@Data
public class SeckillSessionVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "America/Los_Angeles")
    private Date startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "America/Los_Angeles")
    private Date endTime;
    private Integer status;
}
