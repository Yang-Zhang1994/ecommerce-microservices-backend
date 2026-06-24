package com.atguigu.gulimall.order.job;

import com.atguigu.gulimall.order.entity.MqMessageEntity;
import com.atguigu.gulimall.order.repository.MqMessageRepository;
import com.atguigu.gulimall.order.service.MqOutboxDispatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Polls {@code oms_mq_message} with status NEW and publishes to RabbitMQ; marks SENT on success.
 * Retries on broker failures by leaving status NEW.
 */
@Component
public class MqOutboxSenderJob {

    private static final Logger log = LoggerFactory.getLogger(MqOutboxSenderJob.class);

    @Autowired
    private MqMessageRepository mqMessageRepository;
    @Autowired
    private MqOutboxDispatchService mqOutboxDispatchService;

    @Value("${order.mq-outbox.batch-size:100}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${order.mq-outbox.scan-interval-ms:5000}")
    public void flushPending() {
        var page = mqMessageRepository.findByMessageStatusOrderByCreateTimeAsc(
                MqMessageEntity.STATUS_NEW,
                PageRequest.of(0, Math.max(1, batchSize))
        );
        for (MqMessageEntity row : page.getContent()) {
            try {
                mqOutboxDispatchService.sendAndMarkSent(row.getMessageId());
            } catch (Exception e) {
                if (log.isWarnEnabled()) {
                    log.warn("outbox send failed messageId={} exchange={} routingKey={}",
                            row.getMessageId(), row.getToExchange(), row.getRoutingKey(), e);
                }
            }
        }
    }
}
