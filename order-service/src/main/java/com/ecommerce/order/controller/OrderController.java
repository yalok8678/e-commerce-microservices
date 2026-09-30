package com.ecommerce.order.controller;

import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.service.OrderService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ======================================================
    // CREATE ORDER
    // ======================================================

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader,
            Authentication authentication) {

        Long userId =
                (Long) authentication.getPrincipal();

        OrderResponse response =
                orderService.createOrder(
                        userId,
                        authorizationHeader,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // ======================================================
    // GET MY ORDERS
    // ======================================================

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            Authentication authentication) {

        Long userId =
                (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                orderService.getMyOrders(userId)
        );
    }

    // ======================================================
    // GET SINGLE ORDER
    // ======================================================

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable Long orderId,
            Authentication authentication) {

        Long userId =
                (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                orderService.getOrder(
                        userId,
                        orderId
                )
        );
    }

    // ======================================================
    // UPDATE ORDER STATUS
    // ======================================================

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long orderId,
            @RequestParam String status,
            Authentication authentication) {

        Long userId =
                (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                orderService.updateStatus(
                        userId,
                        orderId,
                        status
                )
        );
    }
}