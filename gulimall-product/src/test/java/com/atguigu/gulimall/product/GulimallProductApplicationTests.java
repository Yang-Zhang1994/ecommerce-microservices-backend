package com.atguigu.gulimall.product;

import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

@Slf4j
@SpringBootTest
class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;
    @Autowired
    CategoryService categoryService;

    @Test
    public void testFindPath() {
        Long[] catelogPath = categoryService.findCatelogPath(264L);
        log.info("The Complete Path: {}", Arrays.asList(catelogPath));
    }

    @Test
    void contextLoads() {
        BrandEntity brand = brandService.getById(1L);
        if (brand != null) {
            System.out.println(brand);
        }
//        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setBrandId(1L);
//        brandEntity.setDescript("Apple Inc Description.");
//        brandService.updateById(brandEntity);
//        brandEntity.setName("Apple");
//        brandService.save(brandEntity);
//        System.out.println("Saved Successfully");


    }

}

