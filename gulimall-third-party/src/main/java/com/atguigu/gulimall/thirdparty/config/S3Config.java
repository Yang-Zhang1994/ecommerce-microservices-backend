package com.atguigu.gulimall.thirdparty.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * AWS S3 client and presigner for presigned URL upload (e.g. Canada / North America).
 * Enable when aws.s3.bucket is set.
 * Credentials: aws.access-key + aws.secret-key in config, or env AWS_ACCESS_KEY_ID/AWS_SECRET_ACCESS_KEY, or ~/.aws/credentials.
 */
@Configuration
public class S3Config {

    @Value("${aws.access-key:}")
    private String accessKey;

    @Value("${aws.secret-key:}")
    private String secretKey;

    private AwsCredentialsProvider credentialsProvider() {
        // 优先使用环境变量，避免 YAML 中 secret-key 含 + 等字符被误解析导致 SignatureDoesNotMatch
        String envAccessKey = System.getenv("AWS_ACCESS_KEY_ID");
        String envSecretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
        if (envAccessKey != null && !envAccessKey.isEmpty() && envSecretKey != null && !envSecretKey.isEmpty()) {
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(envAccessKey, envSecretKey));
        }
        if (accessKey != null && !accessKey.trim().isEmpty() && secretKey != null && !secretKey.trim().isEmpty()) {
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey.trim(), secretKey.trim()));
        }
        return DefaultCredentialsProvider.create();
    }

    @Bean
    @ConditionalOnProperty(name = "aws.s3.bucket")
    public S3Client s3Client(@Value("${aws.region:us-east-1}") String region) {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider())
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "aws.s3.bucket")
    public S3Presigner s3Presigner(@Value("${aws.region:us-east-1}") String region) {
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider())
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }
}
