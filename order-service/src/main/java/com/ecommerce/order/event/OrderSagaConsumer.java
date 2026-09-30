package com.ecommerce.order.event;

import com.ecommerce.order.entity.Order;
import com.ecommerce.order.repository.OrderRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderSagaConsumer {

    private final OrderRepository orderRepository;

    private final ObjectMapper objectMapper;


    // ======================================================
    // CONSUME SAGA EVENTS
    // ======================================================

    @KafkaListener(
            topics = "ecommerce.order-events",
            groupId = "order-service"
    )
    public void consumeSagaEvent(
            DomainEvent event) {

        if (event == null ||
                event.getEventType() == null) {

            log.warn(
                    "Order Service received invalid Saga event"
            );

            return;
        }

        switch (event.getEventType()) {

            case "PAYMENT_SUCCESS":

                handlePaymentSuccess(event);

                break;

            case "INVENTORY_RELEASED":
                handleInventoryReleased(event);
                break;

            default:

                log.debug(
                        "Order Service ignoring event: {}",
                        event.getEventType()
                );
        }
    }


    // ======================================================
    // PAYMENT SUCCESS
    // ======================================================

    @Transactional
    protected void handlePaymentSuccess(
            DomainEvent event) {

        PaymentSuccessEvent paymentSuccess =
                objectMapper.convertValue(
                        event.getPayload(),
                        PaymentSuccessEvent.class
                );

        Long orderId =
                paymentSuccess.getOrderId();

        log.info(
                "Order Service received PAYMENT_SUCCESS: " +
                        "eventId={}, orderId={}",
                event.getEventId(),
                orderId
        );

        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Order not found: " + orderId
                                )
                        );

        if ("CONFIRMED".equals(order.getStatus())) {

            log.info(
                    "Order already CONFIRMED: orderId={}",
                    orderId
            );

            return;
        }

        order.setStatus("CONFIRMED");

        orderRepository.save(order);

        log.info(
                "Order confirmed successfully: orderId={}",
                orderId
        );
    }


    // ======================================================
    // PAYMENT FAILED
    // ======================================================

    private void handleInventoryReleased(
            DomainEvent event) {

        Long orderId =
                Long.valueOf(
                        event.getAggregateId()
                );

        log.info(
                "Order Service received INVENTORY_RELEASED: " +
                        "eventId={}, orderId={}",
                event.getEventId(),
                orderId
        );

        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Order not found: " + orderId
                                )
                        );

        if ("CANCELLED".equals(order.getStatus())) {

            log.info(
                    "Order already CANCELLED: orderId={}",
                    orderId
            );

            return;
        }

        order.setStatus("CANCELLED");

        orderRepository.save(order);

        log.info(
                "Order cancelled after inventory compensation: orderId={}",
                orderId
        );
    }
}