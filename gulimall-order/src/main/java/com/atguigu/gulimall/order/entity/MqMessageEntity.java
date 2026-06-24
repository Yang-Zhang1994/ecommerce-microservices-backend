package com.atguigu.gulimall.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Reliable outbound MQ (same transaction as order writes); {@link com.atguigu.gulimall.order.job.MqOutboxSenderJob} flushes pending rows.
 */
@Data
@Entity
@Table(name = "oms_mq_message")
public class MqMessageEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 0 = pending dispatch, 2 = publish submitted awaiting broker confirm, 1 = broker ack (publisher confirm) */
    public static final int STATUS_NEW = 0;
    public static final int STATUS_SENT = 1;
    public static final int STATUS_PENDING_CONFIRM = 2;

    @Id
    @Column(name = "message_id", length = 32, nullable = false)
    private String messageId;

    @Column(name = "content", columnDefinition = "text")
    private String content;

    @Column(name = "to_exchange")
    private String toExchange;

    @Column(name = "routing_key")
    private String routingKey;

    @Column(name = "class_type")
    private String classType;

    @Column(name = "message_status")
    private Integer messageStatus = STATUS_NEW;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;

    @PrePersist
    void prePersist() {
        Date now = new Date();
        if (createTime == null) {
            createTime = now;
        }
        updateTime = now;
        if (messageStatus == null) {
            messageStatus = STATUS_NEW;
        }
    }

    @PreUpdate
    void preUpdate() {
        updateTime = new Date();
    }
}
