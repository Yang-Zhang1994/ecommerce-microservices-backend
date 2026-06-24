package com.atguigu.gulimall.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthInfoController {

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of(
                "service", "gulimall-auth-server",
                "status", "UP");
    }
}
