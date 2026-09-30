package com.ecommerce.order.event;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainEvent<T> {

    private String eventId;

    private String eventType;

    private Instant timestamp;

    private String aggregateType;

    private String aggregateId;

    private T payload;
}