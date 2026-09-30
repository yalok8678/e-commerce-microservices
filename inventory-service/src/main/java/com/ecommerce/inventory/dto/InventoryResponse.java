package com.ecommerce.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InventoryResponse {

    private Long id;
    private Long productId;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private Integer totalQuantity;
}