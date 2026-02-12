/**
  * Copyright 2025 bejson.com 
  */
package com.atguigu.gulimall.product.vo;

import lombok.Data;

/**
 * Auto-generated: 2025-12-25 15:49:10
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class Images {

    private String imgUrl;
    private int defaultImg;

    // 无参构造器
    public Images() {}

    // 单参数构造器，解决 Jackson 无法将 String 转为 Images 对象的问题
    public Images(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}