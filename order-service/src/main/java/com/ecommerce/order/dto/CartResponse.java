package com.ecommerce.order.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

    private Long userId;

    private List<CartItem> items;

    private BigDecimal totalAmount;
}