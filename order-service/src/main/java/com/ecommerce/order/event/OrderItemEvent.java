package com.ecommerce.order.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemEvent {

    private Long productId;

    private String productName;

    private Integer quantity;
}