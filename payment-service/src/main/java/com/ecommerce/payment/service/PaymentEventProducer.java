package com.ecommerce.payment.service;

import com.ecommerce.payment.dto.DomainEvent;
import com.ecommerce.payment.dto.PaymentFailedEvent;
import com.ecommerce.payment.dto.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentEventProducer {

    private static final String TOPIC =
            "ecommerce.order-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentSuccess(
            Long orderId,
            Long userId) {

        PaymentSuccessEvent payload =
                PaymentSuccessEvent.builder()
                        .orderId(orderId)
                        .userId(userId)
                        .status("PAYMENT_SUCCESS")
                        .build();

        DomainEvent event =
                DomainEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .eventType("PAYMENT_SUCCESS")
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

        logEvent(
                "PAYMENT_SUCCESS",
                orderId
        );
    }

    public void publishPaymentFailed(
            Long orderId,
            Long userId,
            String reason) {

        PaymentFailedEvent payload =
                PaymentFailedEvent.builder()
                        .orderId(orderId)
                        .userId(userId)
                        .reason(reason)
                        .build();

        DomainEvent event =
                DomainEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .eventType("PAYMENT_FAILED")
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

        logEvent(
                "PAYMENT_FAILED",
                orderId
        );
    }

    private void logEvent(
            String eventType,
            Long orderId) {

        System.out.println(
                "Published " +
                        eventType +
                        ": orderId=" +
                        orderId
        );
    }
}