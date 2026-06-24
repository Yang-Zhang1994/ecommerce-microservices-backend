package com.atguigu.gulimall.order.service;

import com.atguigu.gulimall.order.dao.StripeWebhookEventRepository;
import com.atguigu.gulimall.order.entity.StripeWebhookEventEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Optional;

@Service
public class StripeWebhookStoreService {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookStoreService.class);

    public static final String STATUS_RECEIVED = "RECEIVED";
    public static final String STATUS_PROCESSED = "PROCESSED";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_SKIPPED = "SKIPPED";

    private final StripeWebhookEventRepository repository;
    private final ObjectMapper objectMapper;
    private final Counter webhookFailedCounter;

    public StripeWebhookStoreService(
            StripeWebhookEventRepository repository,
            ObjectMapper objectMapper,
            @org.springframework.beans.factory.annotation.Autowired(required = false)
            MeterRegistry meterRegistry) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.webhookFailedCounter =
                meterRegistry != null ? meterRegistry.counter("stripe.webhook.failed") : null;
    }

    public boolean isAlreadyProcessed(String eventId) {
        if (!StringUtils.hasText(eventId)) {
            return false;
        }
        return repository.findByEventId(eventId.trim())
                .map(row -> STATUS_PROCESSED.equals(row.getStatus()))
                .orElse(false);
    }

    @Transactional
    public StripeWebhookEventEntity saveReceived(String payload) {
        String eventId = extractEventId(payload);
        String eventType = extractEventType(payload);
        if (!StringUtils.hasText(eventId)) {
            eventId = "unknown-" + Instant.now().toEpochMilli();
        }
        Optional<StripeWebhookEventEntity> existing = repository.findByEventId(eventId);
        StripeWebhookEventEntity row = existing.orElseGet(StripeWebhookEventEntity::new);
        row.setEventId(eventId);
        row.setEventType(eventType);
        row.setPayload(payload);
        if (!STATUS_PROCESSED.equals(row.getStatus())) {
            row.setStatus(STATUS_RECEIVED);
        }
        row.setReceivedAt(row.getReceivedAt() != null ? row.getReceivedAt() : Instant.now());
        return repository.save(row);
    }

    @Transactional
    public void markProcessed(StripeWebhookEventEntity row) {
        row.setStatus(STATUS_PROCESSED);
        row.setProcessedAt(Instant.now());
        repository.save(row);
    }

    @Transactional
    public void markFailed(StripeWebhookEventEntity row, String message) {
        row.setStatus(STATUS_FAILED);
        row.setErrorMessage(message != null && message.length() > 500 ? message.substring(0, 500) : message);
        row.setProcessedAt(Instant.now());
        repository.save(row);
        if (webhookFailedCounter != null) {
            webhookFailedCounter.increment();
        }
        log.error("Stripe webhook failed eventId={} msg={}", row.getEventId(), message);
    }

    @Transactional
    public void markSkipped(StripeWebhookEventEntity row, String reason) {
        row.setStatus(STATUS_SKIPPED);
        row.setErrorMessage(reason);
        row.setProcessedAt(Instant.now());
        repository.save(row);
    }

    public Optional<StripeWebhookEventEntity> findByEventId(String eventId) {
        return repository.findByEventId(eventId);
    }

    private String extractEventId(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            return root.path("id").asText(null);
        } catch (Exception e) {
            return null;
        }
    }

    private String extractEventType(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            return root.path("type").asText(null);
        } catch (Exception e) {
            return null;
        }
    }
}
