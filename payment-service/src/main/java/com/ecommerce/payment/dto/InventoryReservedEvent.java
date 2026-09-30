package com.ecommerce.payment.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryReservedEvent {

    private Long orderId;

    private Long userId;

    private BigDecimal totalAmount;

    private String status;
}