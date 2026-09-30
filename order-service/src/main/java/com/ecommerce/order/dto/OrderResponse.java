package com.ecommerce.order.dto;

import com.ecommerce.order.entity.Order;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
public class OrderResponse {

    private final Long orderId;
    private final Long userId;
    private final BigDecimal totalAmount;
    private final String status;
    private final String shippingAddress;
    private final Instant createdAt;
    private final List<OrderItemResponse> items;

    public OrderResponse(Order order) {

        this.orderId = order.getId();
        this.userId = order.getUserId();
        this.totalAmount = order.getTotalAmount();
        this.status = order.getStatus();
        this.shippingAddress = order.getShippingAddress();
        this.createdAt = order.getCreatedAt();

        this.items = order.getItems()
                .stream()
                .map(OrderItemResponse::new)
                .toList();
    }

    @Getter
    public static class OrderItemResponse {

        private final Long productId;
        private final String productName;
        private final BigDecimal price;
        private final Integer quantity;
        private final BigDecimal subtotal;

        public OrderItemResponse(
                com.ecommerce.order.entity.OrderItem item) {

            this.productId = item.getProductId();
            this.productName = item.getProductName();
            this.price = item.getPrice();
            this.quantity = item.getQuantity();
            this.subtotal = item.getSubtotal();
        }
    }
}