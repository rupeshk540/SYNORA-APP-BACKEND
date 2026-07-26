package com.synora.services.impl;

import com.synora.exception.RateLimitExceededException;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<Long, Bucket> userBuckets = new ConcurrentHashMap<>();

    public void checkLimit(Long userId) {
        Bucket bucket = userBuckets.computeIfAbsent(userId, id -> newBucket());
        if (!bucket.tryConsume(1)) {
            throw new RateLimitExceededException(
                    "AI request limit reached. Please wait a moment before trying again.");
        }
    }

    private Bucket newBucket() {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(5).refillGreedy(5, Duration.ofMinutes(1)))
                .addLimit(limit -> limit.capacity(50).refillGreedy(50, Duration.ofDays(1)))
                .build();
    }
}
