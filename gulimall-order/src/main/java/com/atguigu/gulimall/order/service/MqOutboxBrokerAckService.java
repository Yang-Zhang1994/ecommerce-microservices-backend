package com.atguigu.gulimall.order.service;

import com.atguigu.gulimall.order.entity.MqMessageEntity;
import com.atguigu.gulimall.order.repository.MqMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Updates {@code oms_mq_message} from publisher confirms / returns (requires correlated confirms).
 */
@Service
public class MqOutboxBrokerAckService {

    private static final Logger log = LoggerFactory.getLogger(MqOutboxBrokerAckService.class);

    @Autowired
    private MqMessageRepository mqMessageRepository;

    @Transactional(rollbackFor = Exception.class)
    public void onPublisherConfirm(CorrelationData correlationData, boolean ack, String cause) {
        if (correlationData == null || correlationData.getId() == null) {
            return;
        }
        String messageId = correlationData.getId();
        Date now = new Date();
        if (ack) {
            int updated = mqMessageRepository.updateStatusIfMatch(
                    messageId,
                    MqMessageEntity.STATUS_SENT,
                    MqMessageEntity.STATUS_PENDING_CONFIRM,
                    now
            );
            if (updated == 0) {
                mqMessageRepository.updateStatusIfMatch(
                        messageId,
                        MqMessageEntity.STATUS_SENT,
                        MqMessageEntity.STATUS_NEW,
                        now
                );
            }
        } else {
            int u = mqMessageRepository.updateStatusIfMatch(
                    messageId,
                    MqMessageEntity.STATUS_NEW,
                    MqMessageEntity.STATUS_PENDING_CONFIRM,
                    now
            );
            if (log.isWarnEnabled()) {
                log.warn("publisher confirm nack messageId={} cause={} rows={}", messageId, cause, u);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void onPublishReturned(ReturnedMessage returned) {
        if (returned == null || returned.getMessage() == null) {
            return;
        }
        String correlationId = returned.getMessage().getMessageProperties().getCorrelationId();
        if (correlationId == null) {
            if (log.isWarnEnabled()) {
                log.warn("publish returned: no correlationId replyCode={} replyText={} exchange={} routingKey={}",
                        returned.getReplyCode(),
                        returned.getReplyText(),
                        returned.getExchange(),
                        returned.getRoutingKey());
            }
            return;
        }
        Date now = new Date();
        int u = mqMessageRepository.updateStatusIfMatch(
                correlationId,
                MqMessageEntity.STATUS_NEW,
                MqMessageEntity.STATUS_PENDING_CONFIRM,
                now
        );
        if (log.isWarnEnabled()) {
            log.warn("publish returned messageId={} resetRows={} replyCode={} replyText={}",
                    correlationId, u, returned.getReplyCode(), returned.getReplyText());
        }
    }
}
