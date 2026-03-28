package com.coupons.campaigns.infra.messaging;

import com.coupons.campaigns.domain.OutboxStatus;
import com.coupons.campaigns.domain.entity.OutboxEvent;
import com.coupons.campaigns.infra.persistence.OutboxEventRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxPublisherService {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisherService(
            OutboxEventRepository outboxEventRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelayString = "${coupons.outbox.publish-delay-ms:3000}")
    @Transactional
    public void publishPending() {
        List<OutboxEvent> pending = outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        for (OutboxEvent event : pending) {
            try {
                kafkaTemplate.send(event.getTopic(), event.getEventKey(), event.getPayloadJson()).get();
                event.setStatus(OutboxStatus.SENT);
                event.setSentAt(Instant.now());
                event.setLastError(null);
            } catch (Exception ex) {
                event.setRetryCount(event.getRetryCount() + 1);
                event.setLastError(trim(ex.getMessage(), 500));
                if (event.getRetryCount() >= 10) {
                    event.setStatus(OutboxStatus.FAILED);
                }
            }
            outboxEventRepository.save(event);
        }
    }

    private static String trim(String v, int max) {
        if (v == null) {
            return null;
        }
        return v.length() <= max ? v : v.substring(0, max);
    }
}

