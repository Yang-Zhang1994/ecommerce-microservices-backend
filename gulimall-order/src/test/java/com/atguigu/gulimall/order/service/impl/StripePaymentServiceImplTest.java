package com.atguigu.gulimall.order.service.impl;

import com.atguigu.gulimall.order.repository.OrderRepository;
import com.atguigu.gulimall.order.repository.PaymentInfoRepository;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.service.StripeWebhookStoreService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StripePaymentServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentInfoRepository paymentInfoRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private StripeWebhookStoreService webhookStoreService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private StripePaymentServiceImpl stripePaymentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(stripePaymentService, "stripeWebhookSecret", "whsec_test_secret");
    }

    @Test
    void handleWebhook_skipsPersistenceWhenEventAlreadyProcessed() {
        Event event = org.mockito.Mockito.mock(Event.class);
        when(event.getId()).thenReturn("evt_duplicate_123");
        when(webhookStoreService.isAlreadyProcessed("evt_duplicate_123")).thenReturn(true);

        try (MockedStatic<Webhook> webhook = mockStatic(Webhook.class)) {
            webhook.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                    .thenReturn(event);

            stripePaymentService.handleWebhook("{\"id\":\"evt_duplicate_123\"}", "sig_header");
        }

        verify(webhookStoreService, never()).saveReceived(anyString());
        verify(orderService, never()).markPaidByOrderSn(anyString());
    }
}
