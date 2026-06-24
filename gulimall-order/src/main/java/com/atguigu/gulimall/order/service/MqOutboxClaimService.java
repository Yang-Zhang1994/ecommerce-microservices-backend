package com.atguigu.gulimall.order.service;

import com.atguigu.gulimall.order.entity.MqMessageEntity;
import com.atguigu.gulimall.order.repository.MqMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Commits {@code NEW → PENDING_CONFIRM} in a separate transaction before publish so broker confirm callbacks see persisted state.
 */
@Service
public class MqOutboxClaimService {

    @Autowired
    private MqMessageRepository mqMessageRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public boolean claimNewToPending(String messageId) {
        Date now = new Date();
        return mqMessageRepository.updateStatusIfMatch(
                messageId,
                MqMessageEntity.STATUS_PENDING_CONFIRM,
                MqMessageEntity.STATUS_NEW,
                now
        ) > 0;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void revertPendingToNew(String messageId) {
        Date now = new Date();
        mqMessageRepository.updateStatusIfMatch(
                messageId,
                MqMessageEntity.STATUS_NEW,
                MqMessageEntity.STATUS_PENDING_CONFIRM,
                now
        );
    }
}
