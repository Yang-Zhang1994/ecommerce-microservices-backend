package com.atguigu.gulimall.product;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
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

    /** Disabled by default: requires OSS credentials, network, and local file; run manually when needed. */
    @Test
    @Disabled("Integration test: requires Aliyun OSS credentials and local file; enable manually when needed")
    public void testUpload() throws FileNotFoundException {
        // Endpoint以杭州为例，其它Region请按实际情况填写。
        // 如果你在加拿大使用中国内地的试用包，请确保使用 oss-cn-hangzhou.aliyuncs.com 等内地地址
        String endpoint = "http://oss-cn-hangzhou.aliyuncs.com";

        // 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践
        // 使用环境变量 ALIYUN_ACCESS_KEY_ID 和 ALIYUN_ACCESS_KEY_SECRET，或从配置读取
        String accessKeyId = System.getenv("ALIYUN_ACCESS_KEY_ID");
        String accessKeySecret = System.getenv("ALIYUN_ACCESS_KEY_SECRET");

        // 创建OSSClient实例。
        com.aliyun.oss.OSS ossClient = new com.aliyun.oss.OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 上传文件流。
        // <yourLocalFile>: 你本地电脑上图片的绝对路径，例如 "C:\\Users\\Desktop\\logo.png" 或 "/Users/name/Desktop/logo.png"
        InputStream inputStream = new FileInputStream("/Users/samuelcoe/Downloads/methods_table.png");

        // <yourBucketName>: 你在阿里云创建的 Bucket 名字
        // <yourObjectName>: 你希望在云端保存的文件名，例如 "images/my-logo.png"
        ossClient.putObject("gulimall-a95569", "methods_table.png", inputStream);

        // 关闭OSSClient。
        ossClient.shutdown();

        System.out.println("上传成功！");
    }

}

