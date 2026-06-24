package com.atguigu.common.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Wraps Spring HTTP Interface clients with a Resilience4j circuit breaker.
 */
public final class ResilientHttpClientFactory {

    private ResilientHttpClientFactory() {
    }

    public static <T> T create(
            Class<T> clientType,
            RestClient.Builder restClientBuilder,
            String baseUrl,
            CircuitBreakerRegistry registry,
            String circuitBreakerName) {
        RestClient restClient = restClientBuilder.baseUrl(baseUrl).build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();
        T target = factory.createClient(clientType);
        CircuitBreaker circuitBreaker = registry.circuitBreaker(circuitBreakerName);
        return clientType.cast(Proxy.newProxyInstance(
                clientType.getClassLoader(),
                new Class<?>[]{clientType},
                new CircuitBreakerInvocationHandler(target, circuitBreaker)));
    }

    private record CircuitBreakerInvocationHandler(Object target, CircuitBreaker circuitBreaker)
            implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(target, args);
            }
            return circuitBreaker.executeCallable(() -> {
                try {
                    return method.invoke(target, args);
                } catch (java.lang.reflect.InvocationTargetException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof Exception ex) {
                        throw ex;
                    }
                    if (cause instanceof Error err) {
                        throw err;
                    }
                    throw e;
                }
            });
        }
    }
}
