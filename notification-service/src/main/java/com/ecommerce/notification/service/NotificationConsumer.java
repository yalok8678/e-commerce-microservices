package com.ecommerce.notification.service;

import com.ecommerce.notification.dto.NotificationEvent;

import lombok.RequiredArgsConstructor;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "ecommerce.notifications",
            groupId = "notification-service"
    )
    public void consume(NotificationEvent event) {

        System.out.println(
                "Received Kafka event: "
                        + event.getEventType()
        );

        notificationService.processEvent(event);
    }
}