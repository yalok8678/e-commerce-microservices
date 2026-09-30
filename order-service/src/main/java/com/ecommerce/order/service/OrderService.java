package com.ecommerce.order.service;

import com.ecommerce.order.dto.CartItem;
import com.ecommerce.order.dto.CartResponse;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.event.OrderCreatedEvent;
import com.ecommerce.order.event.OrderItemEvent;
import com.ecommerce.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestClient.Builder restClientBuilder;
    private final OutboxEventService outboxEventService;

    private static final String CART_SERVICE_URL =
            "http://localhost:8085";

    @Transactional
    public OrderResponse createOrder(
            Long userId,
            String authorizationHeader,
            OrderRequest request) {

        // --------------------------------------------------
        // STEP 1: Get cart
        // --------------------------------------------------

        CartResponse cart =
                getCart(authorizationHeader);

        if (cart == null ||
                cart.getItems() == null ||
                cart.getItems().isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cart is empty"
            );
        }

        // --------------------------------------------------
        // STEP 2: Create Order
        //
        // Inventory is NO LONGER reserved here.
        //
        // Inventory reservation is now handled by the
        // Saga through Kafka.
        // --------------------------------------------------

        Order order = Order.builder()
                .userId(userId)
                .shippingAddress(request.getShippingAddress())
                .totalAmount(cart.getTotalAmount())
                .status("PENDING_PAYMENT")
                .createdAt(Instant.now())
                .build();

        // --------------------------------------------------
        // STEP 3: Create Order Items
        // --------------------------------------------------

        List<OrderItem> orderItems =
                cart.getItems()
                        .stream()
                        .map(item -> {

                            BigDecimal subtotal =
                                    item.getPrice()
                                            .multiply(
                                                    BigDecimal.valueOf(
                                                            item.getQuantity()
                                                    )
                                            );

                            return OrderItem.builder()
                                    .order(order)
                                    .productId(item.getProductId())
                                    .productName(item.getProductName())
                                    .price(item.getPrice())
                                    .quantity(item.getQuantity())
                                    .subtotal(subtotal)
                                    .build();
                        })
                        .toList();

        order.setItems(orderItems);

        // --------------------------------------------------
        // STEP 4: Save Order
        // --------------------------------------------------

        Order savedOrder =
                orderRepository.save(order);

        // --------------------------------------------------
        // STEP 5: Create ORDER_CREATED event
        // --------------------------------------------------

        OrderCreatedEvent event =
                OrderCreatedEvent.builder()
                        .orderId(savedOrder.getId())
                        .userId(savedOrder.getUserId())
                        .totalAmount(savedOrder.getTotalAmount())
                        .status(savedOrder.getStatus())
                        .items(
                                savedOrder.getItems()
                                        .stream()
                                        .map(item ->
                                                OrderItemEvent.builder()
                                                        .productId(
                                                                item.getProductId()
                                                        )
                                                        .productName(
                                                                item.getProductName()
                                                        )
                                                        .quantity(
                                                                item.getQuantity()
                                                        )
                                                        .build()
                                        )
                                        .toList()
                        )
                        .build();

        // --------------------------------------------------
        // STEP 6: Save event to Outbox
        // --------------------------------------------------

        outboxEventService.saveOrderCreatedEvent(event);

        // --------------------------------------------------
        // STEP 7: Clear cart
        // --------------------------------------------------

        try {

            clearCart(authorizationHeader);

        } catch (Exception ex) {

            // Order already exists.
            // Cart clearing can be retried later.
            // Do not delete the order.

        }

        // --------------------------------------------------
        // STEP 8: Return Order
        // --------------------------------------------------

        return new OrderResponse(savedOrder);
    }

    // ======================================================
    // GET CART
    // ======================================================

    private CartResponse getCart(
            String authorizationHeader) {

        return restClientBuilder.build()
                .get()
                .uri(
                        CART_SERVICE_URL +
                                "/api/cart"
                )
                .header(
                        "Authorization",
                        authorizationHeader
                )
                .retrieve()
                .body(CartResponse.class);
    }

    // ======================================================
    // CLEAR CART
    // ======================================================

    private void clearCart(
            String authorizationHeader) {

        restClientBuilder.build()
                .delete()
                .uri(
                        CART_SERVICE_URL +
                                "/api/cart"
                )
                .header(
                        "Authorization",
                        authorizationHeader
                )
                .retrieve()
                .toBodilessEntity();
    }

    // ======================================================
    // GET USER ORDERS
    // ======================================================

    public List<OrderResponse> getMyOrders(
            Long userId) {

        return orderRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(OrderResponse::new)
                .toList();
    }

    // ======================================================
    // GET SINGLE ORDER
    // ======================================================

    public OrderResponse getOrder(
            Long userId,
            Long orderId) {

        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Order not found"
                                )
                        );

        if (!order.getUserId().equals(userId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot access this order"
            );
        }

        return new OrderResponse(order);
    }

    // ======================================================
    // UPDATE ORDER STATUS
    // ======================================================

    public OrderResponse updateStatus(
            Long userId,
            Long orderId,
            String status) {

        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Order not found"
                                )
                        );

        if (!order.getUserId().equals(userId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot update this order"
            );
        }

        if ("CONFIRMED".equals(status) &&
                !"PENDING_PAYMENT".equals(order.getStatus())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order is not awaiting payment"
            );
        }

        order.setStatus(status);

        Order savedOrder =
                orderRepository.save(order);

        return new OrderResponse(savedOrder);
    }
}