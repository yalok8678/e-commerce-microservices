package com.ecommerce.notification.dto;

import lombok.*;

import java.time.Instant;
import java.util.Map;

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

    private Map<String, Object> payload;
}