package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.DomainEvent;
import com.ecommerce.inventory.dto.OrderCreatedEvent;
import com.ecommerce.inventory.dto.OrderItemEvent;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final InventoryService inventoryService;

    private final InventoryEventProducer inventoryEventProducer;

    private final ObjectMapper objectMapper;


    // ======================================================
    // CONSUME ORDER EVENTS
    // ======================================================

    @KafkaListener(
            topics = "ecommerce.order-events",
            groupId = "inventory-service"
    )
    public void consumeOrderEvent(
            DomainEvent event) {

        if (event == null ||
                event.getEventType() == null) {

            log.warn(
                    "Inventory Service received invalid event"
            );

            return;
        }

        switch (event.getEventType()) {

            case "ORDER_CREATED":

                handleOrderCreated(event);

                break;

            case "PAYMENT_FAILED":

                handlePaymentFailed(event);

                break;

            default:

                log.debug(
                        "Inventory Service ignoring event: eventType={}",
                        event.getEventType()
                );
        }
    }


    // ======================================================
    // ORDER CREATED
    // ======================================================

    private void handleOrderCreated(
            DomainEvent event) {

        log.info(
                "Inventory Service received ORDER_CREATED: " +
                        "eventId={}, orderId={}",
                event.getEventId(),
                event.getAggregateId()
        );

        OrderCreatedEvent order =
                convertOrderCreatedEvent(event);

        try {

            // --------------------------------------------------
            // Reserve every product in the order
            // --------------------------------------------------

            for (OrderItemEvent item :
                    order.getItems()) {

                inventoryService.reserveStockForOrder(
                        order.getOrderId(),
                        item.getProductId(),
                        item.getQuantity()
                );

                log.info(
                        "Inventory reserved: " +
                                "orderId={}, productId={}, quantity={}",
                        order.getOrderId(),
                        item.getProductId(),
                        item.getQuantity()
                );
            }

            // --------------------------------------------------
            // Continue Saga
            // --------------------------------------------------

            inventoryEventProducer
                    .publishInventoryReserved(
                            order.getOrderId(),
                            order.getUserId(),
                            order.getTotalAmount()
                    );

            log.info(
                    "Inventory reservation completed: " +
                            "orderId={}",
                    order.getOrderId()
            );

        } catch (Exception ex) {

            log.error(
                    "Inventory reservation failed: " +
                            "orderId={}",
                    order.getOrderId(),
                    ex
            );

            throw ex;
        }
    }


    // ======================================================
    // PAYMENT FAILED
    // ======================================================

    private void handlePaymentFailed(
            DomainEvent event) {

        Long orderId =
                Long.valueOf(
                        event.getAggregateId()
                );

        log.warn(
                "Inventory Service received PAYMENT_FAILED: " +
                        "eventId={}, orderId={}",
                event.getEventId(),
                orderId
        );

        try {

            inventoryService.releaseOrderReservations(
                    orderId
            );

            /*
             * Inventory compensation completed.
             *
             * We don't actually need userId for the
             * inventory release operation.
             *
             * The event is only used to notify the next
             * Saga step.
             */

            inventoryEventProducer.publishInventoryReleased(
                    orderId,
                    null
            );

            log.info(
                    "Inventory compensation completed: orderId={}",
                    orderId
            );

        } catch (Exception ex) {

            log.error(
                    "Inventory compensation failed: orderId={}",
                    orderId,
                    ex
            );

            throw ex;
        }
    }


    // ======================================================
    // CONVERT ORDER EVENT
    // ======================================================

    private OrderCreatedEvent convertOrderCreatedEvent(
            DomainEvent event) {

        return objectMapper.convertValue(
                event.getPayload(),
                OrderCreatedEvent.class
        );
    }
}

