package com.ecommerce.notification.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;

    private Long userId;

    private String type;

    private String title;

    private String message;

    private String eventId;

    private String status;

    private Instant createdAt;

    private Instant sentAt;
}