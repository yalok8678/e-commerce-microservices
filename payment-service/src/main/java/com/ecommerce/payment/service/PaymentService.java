package com.ecommerce.payment.service;

import com.ecommerce.payment.dto.CreatePaymentRequest;
import com.ecommerce.payment.dto.OrderResponse;
import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.entity.Payment;
import com.ecommerce.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RestClient.Builder restClientBuilder;

    private static final String ORDER_SERVICE_URL =
            "http://localhost:8086";

    // ======================================================
    // CREATE PAYMENT
    // ======================================================

    public PaymentResponse createPayment(
            Long userId,
            String authorizationHeader,
            CreatePaymentRequest request) {

        // --------------------------------------------------
        // STEP 1: Idempotency check
        // --------------------------------------------------

        var existingPayment =
                paymentRepository.findByIdempotencyKey(
                        request.getIdempotencyKey()
                );

        if (existingPayment.isPresent()) {

            Payment payment =
                    existingPayment.get();

            if (!payment.getUserId().equals(userId)) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Idempotency key belongs to another user"
                );
            }

            return new PaymentResponse(payment);
        }

        // --------------------------------------------------
        // STEP 2: Get order
        // --------------------------------------------------

        OrderResponse order =
                getOrder(
                        request.getOrderId(),
                        authorizationHeader
                );

        if (!order.getUserId().equals(userId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot pay for this order"
            );
        }

        // --------------------------------------------------
        // STEP 3: Validate order status
        // --------------------------------------------------

        if (!"PENDING_PAYMENT".equals(
                order.getStatus())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order is not awaiting payment"
            );
        }

        // --------------------------------------------------
        // STEP 4: Create payment
        // --------------------------------------------------

        Payment payment =
                Payment.builder()
                        .orderId(order.getOrderId())
                        .userId(userId)
                        .amount(order.getTotalAmount())
                        .paymentMethod(
                                request.getPaymentMethod()
                        )
                        .status("PROCESSING")
                        .idempotencyKey(
                                request.getIdempotencyKey()
                        )
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();

        payment = paymentRepository.save(payment);

        // --------------------------------------------------
        // STEP 5: Simulated provider processing
        // --------------------------------------------------

        /*
         * We are not connecting Razorpay/Stripe yet.
         *
         * For now we simulate a successful provider
         * response.
         */

        payment.setStatus("SUCCESS");

        payment.setProviderPaymentId(
                "SIM-" + UUID.randomUUID()
        );

        payment.setUpdatedAt(Instant.now());

        payment =
                paymentRepository.save(payment);

        // --------------------------------------------------
        // STEP 6: Update Order
        // --------------------------------------------------

        try {

            updateOrderStatus(
                    order.getOrderId(),
                    "CONFIRMED",
                    authorizationHeader
            );

        } catch (Exception ex) {

            /*
             * Payment succeeded but order update failed.
             *
             * This is exactly the type of distributed
             * consistency problem that Kafka + Outbox
             * will solve later.
             */

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Payment succeeded but order update failed"
            );
        }

        return new PaymentResponse(payment);
    }

    // ======================================================
    // GET PAYMENT
    // ======================================================

    public PaymentResponse getPayment(
            Long userId,
            Long paymentId) {

        Payment payment =
                paymentRepository.findById(paymentId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Payment not found"
                                )
                        );

        if (!payment.getUserId().equals(userId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot access this payment"
            );
        }

        return new PaymentResponse(payment);
    }

    // ======================================================
    // REFUND
    // ======================================================

    public PaymentResponse refundPayment(
            Long userId,
            Long paymentId) {

        Payment payment =
                paymentRepository.findById(paymentId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Payment not found"
                                )
                        );

        if (!payment.getUserId().equals(userId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot refund this payment"
            );
        }

        if (!"SUCCESS".equals(payment.getStatus())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only successful payments can be refunded"
            );
        }

        payment.setStatus("REFUNDED");
        payment.setUpdatedAt(Instant.now());

        payment =
                paymentRepository.save(payment);

        return new PaymentResponse(payment);
    }

    // ======================================================
    // CALL ORDER SERVICE
    // ======================================================

    private OrderResponse getOrder(
            Long orderId,
            String authorizationHeader) {

        return restClientBuilder.build()
                .get()
                .uri(
                        ORDER_SERVICE_URL +
                                "/api/orders/" +
                                orderId
                )
                .header(
                        "Authorization",
                        authorizationHeader
                )
                .retrieve()
                .body(OrderResponse.class);
    }

    // ======================================================
    // UPDATE ORDER STATUS
    // ======================================================

    private void updateOrderStatus(
            Long orderId,
            String status,
            String authorizationHeader) {

        restClientBuilder.build()
                .put()
                .uri(
                        ORDER_SERVICE_URL +
                                "/api/orders/" +
                                orderId +
                                "/status?status=" +
                                status
                )
                .header(
                        "Authorization",
                        authorizationHeader
                )
                .retrieve()
                .toBodilessEntity();
    }
    // ======================================================
// SAGA PAYMENT PROCESSING
// ======================================================

    public boolean processSagaPayment(
            Long orderId,
            Long userId,
            java.math.BigDecimal amount) {

        // --------------------------------------------------
        // Idempotency check
        // --------------------------------------------------

        var existingPayment =
                paymentRepository.findByOrderId(orderId);

        if (existingPayment.isPresent()) {

            Payment payment =
                    existingPayment.get();

            return "SUCCESS".equals(
                    payment.getStatus()
            );
        }

        // --------------------------------------------------
        // Create payment
        // --------------------------------------------------

        Payment payment =
                Payment.builder()
                        .orderId(orderId)
                        .userId(userId)
                        .amount(amount)
                        .paymentMethod("SAGA")
                        .status("PROCESSING")
                        .idempotencyKey(
                                "SAGA-ORDER-" + orderId
                        )
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();

        payment =
                paymentRepository.save(payment);

        // --------------------------------------------------
        // Simulated provider processing
        // --------------------------------------------------

        /*
         * For the current project we simulate a successful
         * payment provider response.
         *
         * Later this can be replaced with Razorpay/provider
         * integration.
         */

        payment.setStatus("SUCCESS");

        payment.setProviderPaymentId(
                "SAGA-SIM-" + UUID.randomUUID()
        );

        payment.setUpdatedAt(Instant.now());

        paymentRepository.save(payment);

        return true;
    }
}