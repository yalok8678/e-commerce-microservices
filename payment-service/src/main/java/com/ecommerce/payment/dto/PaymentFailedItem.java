package com.ecommerce.payment.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentFailedItem {

    private Long productId;

    private Integer quantity;
}