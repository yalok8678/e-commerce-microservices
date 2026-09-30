package com.ecommerce.order.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderEventProducer {

    private static final String TOPIC = "ecommerce.order-events";

    private final KafkaTemplate<String, DomainEvent<OrderCreatedEvent>> kafkaTemplate;

    public void publishOrderCreated(OrderCreatedEvent event) {

        DomainEvent<OrderCreatedEvent> domainEvent =
                DomainEvent.<OrderCreatedEvent>builder()
                        .eventId(UUID.randomUUID().toString())
                        .eventType("ORDER_CREATED")
                        .timestamp(Instant.now())
                        .aggregateType("ORDER")
                        .aggregateId(String.valueOf(event.getOrderId()))
                        .payload(event)
                        .build();

        kafkaTemplate.send(
                TOPIC,
                String.valueOf(event.getOrderId()),
                domainEvent
        );
    }
}