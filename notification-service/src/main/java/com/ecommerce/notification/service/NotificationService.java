package com.ecommerce.notification.service;

import com.ecommerce.notification.dto.NotificationEvent;
import com.ecommerce.notification.dto.NotificationResponse;
import com.ecommerce.notification.entity.Notification;
import com.ecommerce.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void processEvent(NotificationEvent event) {

        // Idempotency check
        if (notificationRepository
                .findByEventId(event.getEventId())
                .isPresent()) {

            return;
        }

        String title =
                generateTitle(event.getEventType());

        Notification notification =
                Notification.builder()
                        .userId(event.getUserId())
                        .type(event.getEventType())
                        .title(title)
                        .message(event.getMessage())
                        .eventId(event.getEventId())
                        .status("PENDING")
                        .createdAt(Instant.now())
                        .build();

        notification =
                notificationRepository.save(notification);

        sendNotification(notification);
    }

    private void sendNotification(
            Notification notification) {

        try {

            System.out.println(
                    "================================="
            );

            System.out.println(
                    "SENDING NOTIFICATION"
            );

            System.out.println(
                    "User ID: "
                            + notification.getUserId()
            );

            System.out.println(
                    "Type: "
                            + notification.getType()
            );

            System.out.println(
                    "Title: "
                            + notification.getTitle()
            );

            System.out.println(
                    "Message: "
                            + notification.getMessage()
            );

            System.out.println(
                    "================================="
            );

            notification.setStatus("SENT");

            notification.setSentAt(
                    Instant.now()
            );

            notificationRepository.save(
                    notification
            );

        } catch (Exception e) {

            notification.setStatus("FAILED");

            notificationRepository.save(
                    notification
            );

            throw e;
        }
    }

    public List<NotificationResponse>
    getUserNotifications(Long userId) {

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private NotificationResponse mapToResponse(
            Notification notification) {

        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .eventId(notification.getEventId())
                .status(notification.getStatus())
                .createdAt(notification.getCreatedAt())
                .sentAt(notification.getSentAt())
                .build();
    }

    private String generateTitle(
            String eventType) {

        return switch (eventType) {

            case "ORDER_CREATED" ->
                    "Order Created";

            case "ORDER_CONFIRMED" ->
                    "Order Confirmed";

            case "ORDER_CANCELLED" ->
                    "Order Cancelled";

            case "PAYMENT_SUCCESS" ->
                    "Payment Successful";

            case "PAYMENT_FAILED" ->
                    "Payment Failed";

            case "ORDER_SHIPPED" ->
                    "Order Shipped";

            case "ORDER_DELIVERED" ->
                    "Order Delivered";

            default ->
                    "E-Commerce Notification";
        };
    }
}