package com.atguigu.gulimall.thirdparty.controller;

import com.atguigu.common.utils.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * S3 presigned URL for direct browser upload (e.g. Canada / North America).
 * GET /third-party/s3/presigned-url?filename=xxx returns uploadUrl (PUT) and fileUrl (to save).
 */
@RestController
@RequestMapping("/third-party/s3")
@ConditionalOnBean(S3Presigner.class)
public class S3Controller {

    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.region:us-east-1}")
    private String region;

    @Value("${aws.s3.presign-expire-minutes:5}")
    private int presignExpireMinutes = 5;

    @Value("${aws.s3.public-read:true}")
    private boolean publicRead;

    public S3Controller(S3Presigner s3Presigner) {
        this.s3Presigner = s3Presigner;
    }

    @GetMapping("/presigned-url")
    public R presignedUrl(@RequestParam("filename") String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return R.error("filename is required");
        }
        String safeName = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        String prefix = DateTimeFormatter.ISO_LOCAL_DATE.format(java.time.LocalDate.now());
        String key = prefix + "/" + UUID.randomUUID().toString() + "_" + safeName;

        try {
            // 不把 Content-Type 加入签名，避免浏览器发送的 Content-Type 与签名不一致导致 SignatureDoesNotMatch
            PutObjectRequest.Builder putBuilder = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key);
            if (publicRead) {
                putBuilder.acl(ObjectCannedACL.PUBLIC_READ);
            }
            PutObjectRequest putRequest = putBuilder.build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .putObjectRequest(putRequest)
                    .signatureDuration(Duration.ofMinutes(presignExpireMinutes))
                    .build();
            String uploadUrl = s3Presigner.presignPutObject(presignRequest).url().toString();
            String fileUrl = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;

            Map<String, String> data = new LinkedHashMap<>();
            data.put("uploadUrl", uploadUrl);
            data.put("fileUrl", fileUrl);
            data.put("key", key);
            return R.ok().put("data", data);
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            return R.error("S3 presign failed: " + msg);
        }
    }

}
