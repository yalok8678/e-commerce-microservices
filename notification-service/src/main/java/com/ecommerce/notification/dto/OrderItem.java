package com.ecommerce.notification.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    private Long productId;

    private String productName;

    private Integer quantity;
}