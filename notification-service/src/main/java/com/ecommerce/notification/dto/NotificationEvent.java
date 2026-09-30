package com.ecommerce.notification.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent {

    private String eventId;

    private String eventType;

    private Long userId;

    private Long orderId;

    private String message;
}