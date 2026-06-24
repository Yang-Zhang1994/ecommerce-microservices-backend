package com.atguigu.gulimall.gateway.fallback;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Circuit-breaker fallback targets (forward:/fallback/...).
 */
@RestController
@RequestMapping("/fallback")
public class GatewayFallbackController {

    @RequestMapping(value = "/search/**", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Map<String, Object>>> searchFallback() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("products", List.of());
        data.put("pageNum", 1);
        data.put("total", 0);
        data.put("totalPages", 0);
        data.put("degraded", true);
        return Mono.just(ResponseEntity.ok(wrapOk(data)));
    }

    @RequestMapping(value = "/product/**", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Map<String, Object>>> productFallback() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("degraded", true);
        data.put("message", "Product service unavailable; retry later");
        return Mono.just(ResponseEntity.ok(wrapOk(data)));
    }

    @RequestMapping(value = "/seckill/**", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Map<String, Object>>> seckillFallback() {
        // Degrade gracefully: empty flash-sale list so the homepage still renders when seckill is down.
        Map<String, Object> body = wrapOk(List.of());
        body.put("degraded", true);
        return Mono.just(ResponseEntity.ok(body));
    }

    @RequestMapping(value = {"/member/**", "/ware/**", "/order/**", "/auth/**", "/cart/**"},
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Map<String, Object>>> serviceFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(wrapError(503,
                "Service temporarily unavailable")));
    }

    @RequestMapping(value = "/**", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Map<String, Object>>> defaultFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(wrapError(503,
                "Service temporarily unavailable")));
    }

    private static Map<String, Object> wrapOk(Object data) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", 0);
        body.put("msg", "success");
        body.put("data", data);
        return body;
    }

    private static Map<String, Object> wrapError(int code, String msg) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("msg", msg);
        body.put("degraded", true);
        return body;
    }
}
