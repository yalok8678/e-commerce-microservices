package com.ecommerce.order.client;

import com.ecommerce.order.client.InventoryResponse;
import org.springframework.stereotype.Component;

@Component
public class InventoryClientFallback implements InventoryClient {

    @Override
    public InventoryResponse getInventory(Long productId) {

        return InventoryResponse.builder()
                .id(null)
                .productId(productId)
                .availableQuantity(0)
                .reservedQuantity(0)
                .totalQuantity(0)
                .build();
    }

    @Override
    public InventoryResponse reserveStock(
            Long productId,
            Integer quantity
    ) {

        return InventoryResponse.builder()
                .id(null)
                .productId(productId)
                .availableQuantity(0)
                .reservedQuantity(0)
                .totalQuantity(0)
                .build();
    }

    @Override
    public InventoryResponse releaseStock(
            Long productId,
            Integer quantity
    ) {

        return InventoryResponse.builder()
                .id(null)
                .productId(productId)
                .availableQuantity(0)
                .reservedQuantity(0)
                .totalQuantity(0)
                .build();
    }
}