package com.atguigu.common.cache;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.core.StringRedisTemplate;

/** Services without Redis on the classpath (e.g. a new service from `gm new service`) must still start. */
class ProtectedCacheConfigTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class, ProtectedCacheConfig.class));

    @Test
    void skippedWhenRedisIsNotOnTheClasspath() {
        runner.withClassLoader(new FilteredClassLoader(StringRedisTemplate.class))
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(ProtectedCacheConfig.class);
                });
    }

    @Test
    void noCacheWithoutARedisTemplateBean() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(ProtectedCache.class);
        });
    }
}
