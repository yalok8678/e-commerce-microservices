package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            Authentication authentication,
            @Valid @RequestBody ProductRequest request) {

        Long vendorId =
                (Long) authentication.getPrincipal();

        ProductResponse response =
                productService.createProduct(
                        vendorId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>>
    getAllProducts() {

        return ResponseEntity.ok(
                productService.getAllProducts()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                productService.getProductById(id)
        );
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<ProductResponse>>
    getProductsByVendor(
            @PathVariable Long vendorId) {

        return ResponseEntity.ok(
                productService.getProductsByVendor(
                        vendorId
                )
        );
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductResponse>>
    getProductsByCategory(
            @PathVariable String category) {

        return ResponseEntity.ok(
                productService.getProductsByCategory(
                        category
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody ProductRequest request) {

        Long vendorId =
                (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                productService.updateProduct(
                        id,
                        vendorId,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(
            @PathVariable Long id,
            Authentication authentication) {

        Long vendorId =
                (Long) authentication.getPrincipal();

        productService.deleteProduct(
                id,
                vendorId
        );

        return ResponseEntity.ok(
                "Product deleted successfully"
        );
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ProductResponse> changeStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        return ResponseEntity.ok(
                productService.changeStatus(
                        id,
                        status
                )
        );
    }
}