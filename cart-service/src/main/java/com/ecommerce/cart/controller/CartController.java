package com.ecommerce.cart.controller;

import com.ecommerce.cart.dto.AddToCartRequest;
import com.ecommerce.cart.dto.CartResponse;
import com.ecommerce.cart.service.CartService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            Authentication authentication) {

        Long userId =
                (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                cartService.getCart(userId)
        );
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(
            Authentication authentication,
            @Valid @RequestBody AddToCartRequest request) {

        Long userId =
                (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                cartService.addToCart(
                        userId,
                        request
                )
        );
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateQuantity(
            Authentication authentication,
            @PathVariable Long productId,
            @RequestParam Integer quantity) {

        if (quantity < 1) {

            throw new RuntimeException(
                    "Quantity must be at least 1"
            );
        }

        Long userId =
                (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                cartService.updateQuantity(
                        userId,
                        productId,
                        quantity
                )
        );
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeFromCart(
            Authentication authentication,
            @PathVariable Long productId) {

        Long userId =
                (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                cartService.removeFromCart(
                        userId,
                        productId
                )
        );
    }

    @DeleteMapping
    public ResponseEntity<String> clearCart(
            Authentication authentication) {

        Long userId =
                (Long) authentication.getPrincipal();

        cartService.clearCart(userId);

        return ResponseEntity.ok(
                "Cart cleared successfully"
        );
    }
}