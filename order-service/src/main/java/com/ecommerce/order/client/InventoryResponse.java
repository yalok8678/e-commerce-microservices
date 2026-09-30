package com.ecommerce.order.client;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponse {

    private Long id;

    private Long productId;

    private Integer availableQuantity;

    private Integer reservedQuantity;

    private Integer totalQuantity;
}