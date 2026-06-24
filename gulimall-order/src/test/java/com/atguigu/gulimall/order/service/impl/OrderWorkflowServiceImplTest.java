package com.atguigu.gulimall.order.service.impl;

import com.atguigu.gulimall.order.interceptor.OrderLoginInterceptor;
import com.atguigu.gulimall.order.repository.OrderItemRepository;
import com.atguigu.gulimall.order.repository.OrderRepository;
import com.atguigu.gulimall.order.service.OrderMqOutboxService;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.to.OrderLoginUserTo;
import com.atguigu.gulimall.order.vo.OrderSubmitResponseVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderWorkflowServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private OrderMqOutboxService orderMqOutboxService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private OrderWorkflowServiceImpl workflowService;

    @BeforeEach
    void setUp() {
        workflowService = new OrderWorkflowServiceImpl(
                restTemplate,
                objectMapper,
                stringRedisTemplate,
                orderRepository,
                orderItemRepository,
                orderService,
                orderMqOutboxService);
        ReflectionTestUtils.setField(workflowService, "cartServiceUrl", "http://cart");
        ReflectionTestUtils.setField(workflowService, "productServiceUrl", "http://product");
        ReflectionTestUtils.setField(workflowService, "defaultFreight", BigDecimal.ZERO);
        ReflectionTestUtils.setField(workflowService, "freeShippingThreshold", BigDecimal.ZERO);
    }

    @AfterEach
    void tearDown() {
        OrderLoginInterceptor.THREAD_LOCAL.remove();
    }

    @Test
    void submitOrder_returns401WhenNotLoggedIn() {
        OrderSubmitVo submitVo = new OrderSubmitVo();
        submitVo.setOrderToken("token-1");

        OrderSubmitResponseVo response = workflowService.submitOrder(submitVo, "SESSION=test");

        assertEquals(401, response.getCode());
        assertEquals("Please login first", response.getMsg());
    }

    @Test
    void submitOrder_returnsCode1WhenOrderTokenExpired() {
        loginAs(42L);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.getAndDelete("order:token:42:expired-token")).thenReturn(null);

        OrderSubmitVo submitVo = new OrderSubmitVo();
        submitVo.setOrderToken("expired-token");

        OrderSubmitResponseVo response = workflowService.submitOrder(submitVo, "SESSION=test");

        assertEquals(1, response.getCode());
        assertEquals("Please refresh confirm page", response.getMsg());
    }

    @Test
    void submitOrder_returnsCode3WhenPayAmountChanged() throws Exception {
        loginAs(7L);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.getAndDelete("order:token:7:valid-token")).thenReturn("5.00");

        String cartJson = """
                {
                  "data": {
                    "items": [
                      {
                        "check": true,
                        "skuId": 1,
                        "title": "Phone",
                        "count": 1,
                        "price": 10.00,
                        "totalPrice": 10.00
                      }
                    ]
                  }
                }
                """;
        when(restTemplate.exchange(
                eq("http://cart/cart/current"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(ResponseEntity.ok(cartJson));

        OrderSubmitVo submitVo = new OrderSubmitVo();
        submitVo.setOrderToken("valid-token");
        submitVo.setAddressId(1L);

        OrderSubmitResponseVo response = workflowService.submitOrder(submitVo, "SESSION=test");

        assertEquals(3, response.getCode());
        assertEquals("Order price changed, please refresh confirm page", response.getMsg());
        assertEquals(new BigDecimal("10.00"), response.getPayAmount());
    }

    private static void loginAs(long userId) {
        OrderLoginUserTo user = new OrderLoginUserTo();
        user.setUserId(userId);
        user.setUsername("tester");
        user.setIntegration(0);
        OrderLoginInterceptor.THREAD_LOCAL.set(user);
    }
}
