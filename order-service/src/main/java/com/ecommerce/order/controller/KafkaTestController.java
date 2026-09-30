package com.ecommerce.order.controller;

import com.ecommerce.order.event.OrderCreatedEvent;
import com.ecommerce.order.event.OrderEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/test/kafka")
@RequiredArgsConstructor
public class KafkaTestController {

    private final OrderEventProducer orderEventProducer;

    @PostMapping("/order-created")
    public ResponseEntity<String> publishOrderCreatedEvent(
            @RequestParam Long orderId,
            @RequestParam Long userId,
            @RequestParam BigDecimal totalAmount
    ) {

        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(orderId)
                .userId(userId)
                .totalAmount(totalAmount)
                .status("CREATED")
                .build();

        orderEventProducer.publishOrderCreated(event);

        return ResponseEntity.ok(
                "OrderCreatedEvent published successfully"
        );
    }
}