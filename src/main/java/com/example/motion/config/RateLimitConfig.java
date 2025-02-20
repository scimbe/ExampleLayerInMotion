package com.example.motion.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {
    
    private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> apiKeyBuckets = new ConcurrentHashMap<>();

    public Bucket resolveBucketForIp(String ip) {
        return ipBuckets.computeIfAbsent(ip, this::createIpBucket);
    }

    public Bucket resolveBucketForApiKey(String apiKey) {
        return apiKeyBuckets.computeIfAbsent(apiKey, this::createApiKeyBucket);
    }

    private Bucket createIpBucket(String ip) {
        return Bucket4j.builder()
            .addLimit(Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1))))
            .build();
    }

    private Bucket createApiKeyBucket(String apiKey) {
        return Bucket4j.builder()
            .addLimit(Bandwidth.classic(1000, Refill.intervally(1000, Duration.ofHours(1))))
            .build();
    }
}