package com.ecommerce.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class CartResponse {

    private Long userId;

    private List<CartItem> items;

    private BigDecimal totalAmount;
}