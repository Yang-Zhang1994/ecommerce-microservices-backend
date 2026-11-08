package com.atguigu.gulimall.product.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.autoconfigure.ConfigurationPropertiesRebinderAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.cloud.consul.config.ConsulConfigAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import com.atguigu.common.cache.ProtectedCacheProperties;

/**
 * Proves the cache tunables are distributed configuration: values come from Consul KV,
 * and changing a key updates the running app without a restart.
 *
 * Needs a Consul agent (docker compose up -d consul); skipped when none is reachable.
 * Uses a random KV prefix so it never touches the real config/ keys.
 */
class ConsulConfigLiveReloadTest {

    private static final String CONSUL = System.getProperty("consul.url", "http://localhost:8500");
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

    private final String prefix = "it-config-" + UUID.randomUUID().toString().substring(0, 8);
    private ConfigurableApplicationContext ctx;

    @EnableConfigurationProperties(ProtectedCacheProperties.class)
    @ImportAutoConfiguration({
            ConsulAutoConfiguration.class,
            ConsulConfigAutoConfiguration.class,
            RefreshAutoConfiguration.class,
            ConfigurationPropertiesRebinderAutoConfiguration.class})
    static class App {
    }

    @Test
    void cacheTtlChangeInConsulReachesRunningApp() throws Exception {
        assumeTrue(consulUp(), "Consul not reachable at " + CONSUL);
        putYaml("base-ttl: 10m");

        URI uri = URI.create(CONSUL);
        ctx = new SpringApplicationBuilder(App.class)
                .web(WebApplicationType.NONE)
                .run("--spring.config.name=consul-config-live-reload-test", // skip the service's application.yml
                        "--spring.config.import=consul:",
                        "--spring.application.name=gulimall-product",
                        "--spring.cloud.consul.host=" + uri.getHost(),
                        "--spring.cloud.consul.port=" + uri.getPort(),
                        "--spring.cloud.consul.config.prefixes=" + prefix,
                        "--spring.cloud.consul.config.format=YAML",
                        "--spring.cloud.consul.config.data-key=data",
                        "--spring.cloud.consul.config.watch.delay=500",
                        "--spring.cloud.consul.discovery.enabled=false",
                        "--spring.cloud.service-registry.auto-registration.enabled=false");
        ProtectedCacheProperties props = ctx.getBean(ProtectedCacheProperties.class);
        assertEquals(Duration.ofMinutes(10), props.getBaseTtl(), "value loaded from Consul at startup");

        putYaml("base-ttl: 2m");

        long deadline = System.currentTimeMillis() + 15_000;
        while (!Duration.ofMinutes(2).equals(props.getBaseTtl()) && System.currentTimeMillis() < deadline) {
            Thread.sleep(200);
        }
        assertEquals(Duration.ofMinutes(2), props.getBaseTtl(), "running app should pick up the new Consul value");
    }

    @AfterEach
    void cleanUp() throws Exception {
        if (ctx != null) {
            ctx.close();
        }
        if (consulUp()) {
            HTTP.send(HttpRequest.newBuilder(URI.create(CONSUL + "/v1/kv/" + prefix + "?recurse=true")).DELETE().build(),
                    HttpResponse.BodyHandlers.discarding());
        }
    }

    private void putYaml(String cacheProps) throws Exception {
        String yaml = "gulimall:\n  cache:\n    protected:\n      " + cacheProps + "\n";
        HttpResponse<String> res = HTTP.send(
                HttpRequest.newBuilder(URI.create(CONSUL + "/v1/kv/" + prefix + "/application/data"))
                        .PUT(HttpRequest.BodyPublishers.ofString(yaml)).build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, res.statusCode(), res.body());
    }

    private static boolean consulUp() {
        try {
            HttpResponse<String> res = HTTP.send(HttpRequest.newBuilder(URI.create(CONSUL + "/v1/status/leader"))
                    .timeout(Duration.ofSeconds(2)).build(), HttpResponse.BodyHandlers.ofString());
            return res.statusCode() == 200 && res.body().contains(":");
        } catch (Exception e) {
            return false;
        }
    }
}
