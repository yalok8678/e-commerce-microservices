package com.ecommerce.payment.service;

import com.ecommerce.payment.dto.DomainEvent;
import com.ecommerce.payment.dto.InventoryReservedEvent;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final PaymentService paymentService;

    private final PaymentEventProducer paymentEventProducer;

    private final ObjectMapper objectMapper;


    @KafkaListener(
            topics = "ecommerce.order-events",
            groupId = "payment-service"
    )
    public void consumeOrderEvent(
            DomainEvent event) {

        if (event == null ||
                event.getEventType() == null) {

            log.warn(
                    "Payment Service received invalid event"
            );

            return;
        }

        switch (event.getEventType()) {

            case "INVENTORY_RESERVED":

                handleInventoryReserved(event);

                break;

            default:

                log.debug(
                        "Payment Service ignoring event: {}",
                        event.getEventType()
                );
        }
    }


    private void handleInventoryReserved(
            DomainEvent event) {

        InventoryReservedEvent inventoryReserved =
                objectMapper.convertValue(
                        event.getPayload(),
                        InventoryReservedEvent.class
                );

        Long orderId =
                inventoryReserved.getOrderId();

        Long userId =
                inventoryReserved.getUserId();

        log.info(
                "Payment Service received INVENTORY_RESERVED: " +
                        "eventId={}, orderId={}, amount={}",
                event.getEventId(),
                orderId,
                inventoryReserved.getTotalAmount()
        );

        try {

            boolean paymentSuccessful =
                    paymentService.processSagaPayment(
                            orderId,
                            userId,
                            inventoryReserved.getTotalAmount()
                    );

            if (paymentSuccessful) {

                log.info(
                        "Payment successful: orderId={}",
                        orderId
                );

                paymentEventProducer.publishPaymentSuccess(
                        orderId,
                        userId
                );

            } else {

                log.warn(
                        "Payment failed: orderId={}",
                        orderId
                );

                paymentEventProducer.publishPaymentFailed(
                        orderId,
                        userId,
                        "Payment processing failed"
                );
            }

        } catch (Exception ex) {

            log.error(
                    "Payment processing exception: orderId={}",
                    orderId,
                    ex
            );

            paymentEventProducer.publishPaymentFailed(
                    orderId,
                    userId,
                    ex.getMessage() != null
                            ? ex.getMessage()
                            : "Payment processing exception"
            );

            throw ex;
        }
    }
}