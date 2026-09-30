package com.ecommerce.order.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    private Long productId;

    private String productName;

    private BigDecimal price;

    private Integer quantity;
}