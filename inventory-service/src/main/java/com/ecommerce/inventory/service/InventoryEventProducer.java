package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.DomainEvent;
import com.ecommerce.inventory.dto.InventoryReleasedEvent;
import com.ecommerce.inventory.dto.InventoryReservedEvent;

import lombok.RequiredArgsConstructor;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryEventProducer {

    private static final String TOPIC =
            "ecommerce.order-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;


    // ======================================================
    // INVENTORY RESERVED
    // ======================================================

    public void publishInventoryReserved(
            Long orderId,
            Long userId,
            BigDecimal totalAmount) {

        InventoryReservedEvent payload =
                InventoryReservedEvent.builder()
                        .orderId(orderId)
                        .userId(userId)
                        .totalAmount(totalAmount)
                        .status("INVENTORY_RESERVED")
                        .build();

        DomainEvent event =
                DomainEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .eventType("INVENTORY_RESERVED")
                        .timestamp(Instant.now())
                        .aggregateType("ORDER")
                        .aggregateId(
                                String.valueOf(orderId)
                        )
                        .payload(payload)
                        .build();

        kafkaTemplate.send(
                TOPIC,
                String.valueOf(orderId),
                event
        );

        System.out.println(
                "Published INVENTORY_RESERVED: orderId="
                        + orderId
        );
    }


    // ======================================================
    // INVENTORY RELEASED
    // ======================================================

    public void publishInventoryReleased(
            Long orderId,
            Long userId) {

        InventoryReleasedEvent payload =
                InventoryReleasedEvent.builder()
                        .orderId(orderId)
                        .userId(userId)
                        .status("INVENTORY_RELEASED")
                        .build();

        DomainEvent event =
                DomainEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .eventType("INVENTORY_RELEASED")
                        .timestamp(Instant.now())
                        .aggregateType("ORDER")
                        .aggregateId(
                                String.valueOf(orderId)
                        )
                        .payload(payload)
                        .build();

        kafkaTemplate.send(
                TOPIC,
                String.valueOf(orderId),
                event
        );

        System.out.println(
                "Published INVENTORY_RELEASED: orderId="
                        + orderId
        );
    }
}