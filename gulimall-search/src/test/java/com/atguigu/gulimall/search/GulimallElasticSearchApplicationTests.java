package com.atguigu.gulimall.search;

import com.atguigu.gulimall.search.config.GulimallElasticSearchConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

/**
 * 搜索服务测试类
 */
@SpringBootTest
class GulimallElasticSearchApplicationTests {

    @Autowired
    RestHighLevelClient client;

    @Test
    void test() {
        // Test whether elasticsearch client is correctly injected
        System.out.println("Elasticsearch Client: " + client);

    }

    @Test
    void testIndex() throws IOException {
        User user = new User();
        user.setUsername("zhangsan");
        user.setAge(30);
        user.setGender("male");
        String s = new ObjectMapper().writeValueAsString(user);

        IndexRequest request = new IndexRequest("users").id("1")
                //.source("username", "zhangsan", "age", 30, "gender", "male");
                .source(s, XContentType.JSON);

        IndexResponse response = client.index(request, GulimallElasticSearchConfiguration.COMMON_OPTIONS);
        System.out.println(response);


    }

    @Data
    class User {
        private String username;
        private Integer age;
        private String gender;
    }

}

