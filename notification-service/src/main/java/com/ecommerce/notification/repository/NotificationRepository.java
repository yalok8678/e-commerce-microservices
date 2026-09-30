package com.ecommerce.notification.repository;

import com.ecommerce.notification.entity.Notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(
            Long userId
    );

    Optional<Notification> findByEventId(
            String eventId
    );
}