package com.ecommerce.payment.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentFailedEvent {

    private Long orderId;

    private Long userId;

    private String reason;

    private List<PaymentFailedItem> items;
}