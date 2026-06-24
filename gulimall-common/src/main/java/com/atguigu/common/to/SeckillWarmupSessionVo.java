package com.atguigu.common.to;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** One enabled session plus its SKU relations for Redis warm-up. */
@Data
public class SeckillWarmupSessionVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private SeckillSessionVo session;
    private List<SeckillSkuRelationVo> skus = new ArrayList<>();
}
