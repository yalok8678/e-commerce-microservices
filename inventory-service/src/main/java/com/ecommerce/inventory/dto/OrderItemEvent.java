package com.ecommerce.inventory.dto;

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