package com.ecommerce.payment.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainEvent {

    private String eventId;

    private String eventType;

    private Instant timestamp;

    private String aggregateType;

    private String aggregateId;

    private Object payload;
}