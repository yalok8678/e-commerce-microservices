package com.ecommerce.order.client;

import com.ecommerce.order.client.InventoryResponse;
import com.ecommerce.order.config.FeignConfig;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "inventory-service",
        configuration = FeignConfig.class,
        fallback = InventoryClientFallback.class

)
public interface InventoryClient {

    @GetMapping("/api/inventory/{productId}")
    InventoryResponse getInventory(
            @PathVariable("productId") Long productId
    );

    @PostMapping("/api/inventory/{productId}/reserve")
    InventoryResponse reserveStock(
            @PathVariable("productId") Long productId,
            @RequestParam("quantity") Integer quantity
    );

    @PostMapping("/api/inventory/{productId}/release")
    InventoryResponse releaseStock(
            @PathVariable("productId") Long productId,
            @RequestParam("quantity") Integer quantity
    );
}