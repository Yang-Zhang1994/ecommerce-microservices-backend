package com.atguigu.gulimall.search;

import com.atguigu.gulimall.search.config.GulimallElasticSearchConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

/**
 * Search service test class.
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
                .source(s, XContentType.JSON);

        try {
            IndexResponse response = client.index(request, GulimallElasticSearchConfiguration.COMMON_OPTIONS);
            System.out.println(response);
        } catch (IOException e) {
            // When ES 8.x response format is incompatible with 7.x client parsing,
            // treat it as success if the actual HTTP status is 200 OK.
            if (e.getMessage() != null && e.getMessage().contains("200 OK")) {
                System.out.println("Index succeeded (200); ES 8.x response not parsed by 7.x client.");
                return;
            }
            throw e;
        }
    }

    @Data
    class User {
        private String username;
        private Integer age;
        private String gender;
    }

}

