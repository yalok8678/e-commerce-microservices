package com.ecommerce.order.event;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreatedEvent {

    private Long orderId;

    private Long userId;

    private BigDecimal totalAmount;

    private String status;

    private List<OrderItemEvent> items;
}