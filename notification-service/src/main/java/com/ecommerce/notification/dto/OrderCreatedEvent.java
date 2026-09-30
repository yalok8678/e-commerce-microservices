package com.ecommerce.notification.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreatedEvent {

    private Long orderId;

    private Long userId;

    private Double totalAmount;

    private String status;

    private List<OrderItem> items;
}