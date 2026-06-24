package com.atguigu.gulimall.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch client configuration.
 */
@Configuration
public class GulimallElasticSearchConfiguration {

    @Value("${elasticsearch.host:192.168.56.10}")
    private String host;

    @Value("${elasticsearch.port:9200}")
    private int port;

    @Value("${elasticsearch.scheme:http}")
    private String scheme;

    /** ES 8.x REST responses use a format the 7.17 HLRC cannot parse without compatibility headers. */
    private static final String ES_COMPAT_MEDIA = "application/vnd.elasticsearch+json;compatible-with=7";

    public static final RequestOptions COMMON_OPTIONS;
    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        builder.addHeader("Accept", ES_COMPAT_MEDIA);
        builder.addHeader("Content-Type", ES_COMPAT_MEDIA);
        COMMON_OPTIONS = builder.build();
    }

    @Bean
    public RestHighLevelClient esRestClient() {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(host, port, scheme))
        );
        return client;
    }
}
