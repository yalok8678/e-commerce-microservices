package com.ecommerce.order.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSuccessEvent {

    private Long orderId;

    private Long userId;

    private String status;
}