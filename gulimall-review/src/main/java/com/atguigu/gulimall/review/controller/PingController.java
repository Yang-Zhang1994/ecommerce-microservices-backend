package com.atguigu.gulimall.review.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    @Value("${spring.application.name}")
    private String service;

    @GetMapping("/api/review/ping")
    public Map<String, String> ping() {
        return Map.of("service", service, "status", "ok");
    }
}
