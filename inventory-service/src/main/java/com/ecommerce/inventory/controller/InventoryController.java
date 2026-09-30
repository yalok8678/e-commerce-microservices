package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.dto.InventoryRequest;
import com.ecommerce.inventory.dto.InventoryResponse;
import com.ecommerce.inventory.dto.QuantityRequest;
import com.ecommerce.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<InventoryResponse> createInventory(
            @Valid @RequestBody InventoryRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        inventoryService.createInventory(request)
                );
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> getInventory(
            @PathVariable Long productId) {

        return ResponseEntity.ok(
                inventoryService.getInventoryByProductId(
                        productId
                )
        );
    }

    @PutMapping("/{productId}/add")
    public ResponseEntity<InventoryResponse> addStock(
            @PathVariable Long productId,
            @Valid @RequestBody QuantityRequest request) {

        return ResponseEntity.ok(
                inventoryService.addStock(
                        productId,
                        request.getQuantity()
                )
        );
    }

    @PutMapping("/{productId}/remove")
    public ResponseEntity<InventoryResponse> removeStock(
            @PathVariable Long productId,
            @Valid @RequestBody QuantityRequest request) {

        return ResponseEntity.ok(
                inventoryService.removeStock(
                        productId,
                        request.getQuantity()
                )
        );
    }

    @PutMapping("/{productId}/reserve")
    public ResponseEntity<InventoryResponse> reserveStock(
            @PathVariable Long productId,
            @Valid @RequestBody QuantityRequest request) {

        return ResponseEntity.ok(
                inventoryService.reserveStock(
                        productId,
                        request.getQuantity()
                )
        );
    }

    @PutMapping("/{productId}/release")
    public ResponseEntity<InventoryResponse> releaseStock(
            @PathVariable Long productId,
            @Valid @RequestBody QuantityRequest request) {

        return ResponseEntity.ok(
                inventoryService.releaseStock(
                        productId,
                        request.getQuantity()
                )
        );
    }
}