package com.ecommerce.order.service;

import com.ecommerce.order.entity.OutboxEvent;
import com.ecommerce.order.event.DomainEvent;
import com.ecommerce.order.event.OrderCreatedEvent;
import com.ecommerce.order.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisherService {

    private static final String TOPIC = "ecommerce.order-events";

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, DomainEvent<OrderCreatedEvent>> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {

        List<OutboxEvent> events =
                outboxEventRepository
                        .findTop50ByPublishedFalseOrderByCreatedAtAsc();

        for (OutboxEvent outboxEvent : events) {

            try {

                DomainEvent<OrderCreatedEvent> domainEvent =
                        objectMapper.readValue(
                                outboxEvent.getPayload(),
                                new TypeReference<DomainEvent<OrderCreatedEvent>>() {
                                }
                        );

                kafkaTemplate
                        .send(
                                TOPIC,
                                outboxEvent.getAggregateId(),
                                domainEvent
                        )
                        .get();

                outboxEvent.setPublished(true);
                outboxEvent.setPublishedAt(Instant.now());

                outboxEventRepository.save(outboxEvent);

                log.info(
                        "Outbox event published successfully: eventId={}, eventType={}, aggregateId={}",
                        outboxEvent.getEventId(),
                        outboxEvent.getEventType(),
                        outboxEvent.getAggregateId()
                );

            } catch (Exception ex) {

                log.error(
                        "Failed to publish outbox event: eventId={}, aggregateId={}",
                        outboxEvent.getEventId(),
                        outboxEvent.getAggregateId(),
                        ex
                );
            }
        }
    }
}