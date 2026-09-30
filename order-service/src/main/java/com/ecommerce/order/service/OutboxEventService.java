package com.ecommerce.order.service;

import com.ecommerce.order.entity.OutboxEvent;
import com.ecommerce.order.event.DomainEvent;
import com.ecommerce.order.event.OrderCreatedEvent;
import com.ecommerce.order.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public void saveOrderCreatedEvent(OrderCreatedEvent event) {

        try {

            DomainEvent<OrderCreatedEvent> domainEvent =
                    DomainEvent.<OrderCreatedEvent>builder()
                            .eventId(UUID.randomUUID().toString())
                            .eventType("ORDER_CREATED")
                            .timestamp(Instant.now())
                            .aggregateType("ORDER")
                            .aggregateId(String.valueOf(event.getOrderId()))
                            .payload(event)
                            .build();

            String payload =
                    objectMapper.writeValueAsString(domainEvent);

            OutboxEvent outboxEvent =
                    OutboxEvent.builder()
                            .eventId(domainEvent.getEventId())
                            .eventType(domainEvent.getEventType())
                            .aggregateType(domainEvent.getAggregateType())
                            .aggregateId(domainEvent.getAggregateId())
                            .payload(payload)
                            .createdAt(Instant.now())
                            .published(false)
                            .build();

            outboxEventRepository.save(outboxEvent);

        } catch (Exception ex) {

            throw new RuntimeException(
                    "Failed to create outbox event",
                    ex
            );
        }
    }
}