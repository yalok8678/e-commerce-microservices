package com.ecommerce.notification.service;

import com.ecommerce.notification.entity.ProcessedEvent;
import com.ecommerce.notification.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final ProcessedEventRepository processedEventRepository;

    public boolean alreadyProcessed(String eventId) {
        return processedEventRepository.existsByEventId(eventId);
    }

    @Transactional
    public void markAsProcessed(String eventId) {

        if (processedEventRepository.existsByEventId(eventId)) {
            return;
        }

        processedEventRepository.save(
                ProcessedEvent.builder()
                        .eventId(eventId)
                        .processedAt(Instant.now())
                        .build()
        );
    }
}