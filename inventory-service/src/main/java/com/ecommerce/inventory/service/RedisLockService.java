package com.ecommerce.inventory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RedisLockService {

    private final RedisTemplate<String, Object> redisTemplate;

    public String acquireLock(
            String lockKey,
            Duration timeout) {

        String lockValue = UUID.randomUUID().toString();

        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, timeout);

        if (Boolean.TRUE.equals(acquired)) {
            return lockValue;
        }

        return null;
    }

    public void releaseLock(
            String lockKey,
            String lockValue) {

        Object currentValue =
                redisTemplate.opsForValue().get(lockKey);

        if (lockValue.equals(currentValue)) {
            redisTemplate.delete(lockKey);
        }
    }
}