package com.atguigu.gulimall.order.config;

import com.atguigu.gulimall.order.interceptor.OrderLoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class OrderInterceptorConfig implements WebMvcConfigurer {
    private final OrderLoginInterceptor orderLoginInterceptor;

    public OrderInterceptorConfig(OrderLoginInterceptor orderLoginInterceptor) {
        this.orderLoginInterceptor = orderLoginInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Mall checkout/member APIs need SESSION; renren admin CRUD (/order/order/** etc.) uses gateway token, not member login.
        registry.addInterceptor(orderLoginInterceptor)
                .addPathPatterns("/order/**")
                .excludePathPatterns(
                        "/order/ping",
                        "/order/pay/stripe/webhook",
                        "/order/pay/stripe/webhook/replay/**",
                        "/order/post-pay/**",
                        "/order/order/**",
                        "/order/orderitem/**",
                        "/order/paymentinfo/**",
                        "/order/refundinfo/**",
                        "/order/orderreturnreason/**",
                        "/order/orderreturnapply/**",
                        "/order/ordersetting/**",
                        "/order/orderoperatehistory/**");
    }
}
