package com.ecommerce.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class FallbackController {

    @GetMapping("/fallback/product")
    public ResponseEntity<Map<String, Object>>
    productFallback() {

        return buildFallback(
                "Product Service is temporarily unavailable."
        );
    }

    @GetMapping("/fallback/vendor")
    public ResponseEntity<Map<String, Object>>
    vendorFallback() {

        return buildFallback(
                "Vendor Service is temporarily unavailable."
        );
    }

    @GetMapping("/fallback/inventory")
    public ResponseEntity<Map<String, Object>>
    inventoryFallback() {

        return buildFallback(
                "Inventory Service is temporarily unavailable."
        );
    }

    @GetMapping("/fallback/cart")
    public ResponseEntity<Map<String, Object>>
    cartFallback() {

        return buildFallback(
                "Cart Service is temporarily unavailable."
        );
    }

    @GetMapping("/fallback/order")
    public ResponseEntity<Map<String, Object>>
    orderFallback() {

        return buildFallback(
                "Order Service is temporarily unavailable."
        );
    }

    @GetMapping("/fallback/payment")
    public ResponseEntity<Map<String, Object>>
    paymentFallback() {

        return buildFallback(
                "Payment Service is temporarily unavailable."
        );
    }

    @GetMapping("/fallback/notification")
    public ResponseEntity<Map<String, Object>>
    notificationFallback() {

        return buildFallback(
                "Notification Service is temporarily unavailable."
        );
    }

    private ResponseEntity<Map<String, Object>>
    buildFallback(String message) {

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "timestamp",
                Instant.now()
        );

        response.put(
                "status",
                HttpStatus.SERVICE_UNAVAILABLE.value()
        );

        response.put(
                "error",
                "Service Unavailable"
        );

        response.put(
                "message",
                message
        );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }
}