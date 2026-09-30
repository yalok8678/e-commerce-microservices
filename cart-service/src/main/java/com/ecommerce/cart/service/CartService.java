package com.ecommerce.cart.service;

import com.ecommerce.cart.dto.AddToCartRequest;
import com.ecommerce.cart.dto.CartItem;
import com.ecommerce.cart.dto.CartResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CartService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CART_PREFIX = "cart:";

    private static final long CART_TTL_DAYS = 7;

    public CartResponse getCart(Long userId) {

        String cartKey = getCartKey(userId);

        List<CartItem> items =
                getItems(cartKey);

        return buildResponse(userId, items);
    }

    public CartResponse addToCart(
            Long userId,
            AddToCartRequest request) {

        String cartKey = getCartKey(userId);

        List<CartItem> items =
                getItems(cartKey);

        boolean productExists = false;

        for (CartItem item : items) {

            if (item.getProductId()
                    .equals(request.getProductId())) {

                item.setQuantity(
                        item.getQuantity()
                                + request.getQuantity()
                );

                item.setProductName(
                        request.getProductName()
                );

                item.setPrice(
                        request.getPrice()
                );

                productExists = true;

                break;
            }
        }

        if (!productExists) {

            items.add(
                    new CartItem(
                            request.getProductId(),
                            request.getProductName(),
                            request.getPrice(),
                            request.getQuantity()
                    )
            );
        }

        saveItems(cartKey, items);

        return buildResponse(userId, items);
    }

    public CartResponse updateQuantity(
            Long userId,
            Long productId,
            Integer quantity) {

        String cartKey = getCartKey(userId);

        List<CartItem> items =
                getItems(cartKey);

        boolean found = false;

        for (CartItem item : items) {

            if (item.getProductId()
                    .equals(productId)) {

                item.setQuantity(quantity);

                found = true;

                break;
            }
        }

        if (!found) {

            throw new RuntimeException(
                    "Product not found in cart"
            );
        }

        saveItems(cartKey, items);

        return buildResponse(userId, items);
    }

    public CartResponse removeFromCart(
            Long userId,
            Long productId) {

        String cartKey = getCartKey(userId);

        List<CartItem> items =
                getItems(cartKey);

        boolean removed =
                items.removeIf(
                        item ->
                                item.getProductId()
                                        .equals(productId)
                );

        if (!removed) {

            throw new RuntimeException(
                    "Product not found in cart"
            );
        }

        saveItems(cartKey, items);

        return buildResponse(userId, items);
    }

    public void clearCart(Long userId) {

        redisTemplate.delete(
                getCartKey(userId)
        );
    }

    private String getCartKey(Long userId) {

        return CART_PREFIX + userId;
    }

    @SuppressWarnings("unchecked")
    private List<CartItem> getItems(
            String cartKey) {

        Object value =
                redisTemplate.opsForValue()
                        .get(cartKey);

        if (value == null) {

            return new ArrayList<>();
        }

        return (List<CartItem>) value;
    }

    private void saveItems(
            String cartKey,
            List<CartItem> items) {

        redisTemplate.opsForValue()
                .set(
                        cartKey,
                        items,
                        CART_TTL_DAYS,
                        TimeUnit.DAYS
                );
    }

    private CartResponse buildResponse(
            Long userId,
            List<CartItem> items) {

        BigDecimal totalAmount =
                items.stream()
                        .map(item ->
                                item.getPrice()
                                        .multiply(
                                                BigDecimal.valueOf(
                                                        item.getQuantity()
                                                )
                                        )
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        return new CartResponse(
                userId,
                items,
                totalAmount
        );
    }
}