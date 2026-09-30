package com.ecommerce.notification.service;

import com.ecommerce.notification.dto.DomainEvent;
import com.ecommerce.notification.dto.OrderCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final ObjectMapper objectMapper;
    private final IdempotencyService idempotencyService;

    @Value("${app.kafka.test-failure:false}")
    private boolean testFailure;

    /*
     * 4 total attempts:
     *
     * Attempt 1 = original
     * Attempt 2 = retry
     * Attempt 3 = retry
     * Attempt 4 = retry
     *
     * After all attempts fail -> DLT
     */
    @RetryableTopic(
            attempts = "4",
            dltTopicSuffix = ".DLT",
            topicSuffixingStrategy =
                    TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(
            topics = "ecommerce.order-events",
            groupId = "notification-service"
    )
    public void consumeOrderCreated(DomainEvent event) {

        log.info(
                "Received Domain Event: eventId={}, eventType={}, aggregateType={}, aggregateId={}",
                event.getEventId(),
                event.getEventType(),
                event.getAggregateType(),
                event.getAggregateId()
        );

        /*
         * IDempotency
         */
        if (idempotencyService.alreadyProcessed(event.getEventId())) {

            log.warn(
                    "Duplicate event ignored: eventId={}, aggregateId={}",
                    event.getEventId(),
                    event.getAggregateId()
            );

            return;
        }

        /*
         * Convert payload
         */
        OrderCreatedEvent order =
                objectMapper.convertValue(
                        event.getPayload(),
                        OrderCreatedEvent.class
                );

        log.info(
                "Processing ORDER_CREATED: orderId={}, userId={}, totalAmount={}, status={}",
                order.getOrderId(),
                order.getUserId(),
                order.getTotalAmount(),
                order.getStatus()
        );

        /*
         * Deliberate failure for Retry + DLT testing
         */
        if (testFailure) {

            log.error(
                    "Intentional test failure triggered: eventId={}",
                    event.getEventId()
            );

            throw new RuntimeException(
                    "Intentional Notification Service failure for testing"
            );
        }

        /*
         * Simulated notification processing
         */
        log.info(
                "Sending notification for orderId={}",
                order.getOrderId()
        );

        log.info(
                "Notification processing completed: eventId={}, orderId={}",
                event.getEventId(),
                order.getOrderId()
        );

        /*
         * Mark processed only after successful processing
         */
        idempotencyService.markAsProcessed(
                event.getEventId()
        );

        log.info(
                "Event marked as processed: eventId={}",
                event.getEventId()
        );
    }

    /*
     * DLT handler
     */
    @DltHandler
    public void handleDlt(DomainEvent event) {

        log.error("==============================================");
        log.error("EVENT MOVED TO DEAD LETTER TOPIC");
        log.error("eventId={}", event.getEventId());
        log.error("eventType={}", event.getEventType());
        log.error("aggregateType={}", event.getAggregateType());
        log.error("aggregateId={}", event.getAggregateId());
        log.error("payload={}", event.getPayload());
        log.error("==============================================");
    }
}