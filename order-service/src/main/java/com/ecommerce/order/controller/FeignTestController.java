package com.ecommerce.order.controller;

import com.ecommerce.order.client.InventoryClient;
import com.ecommerce.order.client.InventoryResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test/feign")
@RequiredArgsConstructor
public class FeignTestController {

    private final InventoryClient inventoryClient;

    @GetMapping("/inventory/{productId}")
    public InventoryResponse testInventory(
            @PathVariable Long productId) {

        return inventoryClient.getInventory(productId);
    }
}