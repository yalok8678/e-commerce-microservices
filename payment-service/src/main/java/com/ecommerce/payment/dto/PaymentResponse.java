package com.ecommerce.payment.dto;

import com.ecommerce.payment.entity.Payment;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
public class PaymentResponse {

    private final Long paymentId;
    private final Long orderId;
    private final Long userId;
    private final BigDecimal amount;
    private final String paymentMethod;
    private final String status;
    private final String idempotencyKey;
    private final String providerPaymentId;
    private final Instant createdAt;
    private final Instant updatedAt;

    public PaymentResponse(Payment payment) {

        this.paymentId = payment.getId();
        this.orderId = payment.getOrderId();
        this.userId = payment.getUserId();
        this.amount = payment.getAmount();
        this.paymentMethod = payment.getPaymentMethod();
        this.status = payment.getStatus();
        this.idempotencyKey = payment.getIdempotencyKey();
        this.providerPaymentId =
                payment.getProviderPaymentId();
        this.createdAt = payment.getCreatedAt();
        this.updatedAt = payment.getUpdatedAt();
    }
}