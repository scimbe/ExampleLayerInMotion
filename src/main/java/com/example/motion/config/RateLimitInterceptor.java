package com.example.motion.config;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitConfig rateLimitConfig;

    public RateLimitInterceptor(RateLimitConfig rateLimitConfig) {
        this.rateLimitConfig = rateLimitConfig;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String apiKey = request.getHeader("X-API-Key");
        String clientIp = getClientIp(request);

        // Prüfe API Key Rate Limit
        if (apiKey != null) {
            Bucket bucket = rateLimitConfig.resolveBucketForApiKey(apiKey);
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
            
            if (!probe.isConsumed()) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setHeader("X-Rate-Limit-Retry-After-Milliseconds", 
                    String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000));
                return false;
            }
            
            response.setHeader("X-Rate-Limit-Remaining", 
                String.valueOf(probe.getRemainingTokens()));
        }

        // Prüfe IP-basiertes Rate Limit
        Bucket bucket = rateLimitConfig.resolveBucketForIp(clientIp);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (!probe.isConsumed()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("X-Rate-Limit-Retry-After-Milliseconds", 
                String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000));
            return false;
        }

        response.setHeader("X-Rate-Limit-Remaining", 
            String.valueOf(probe.getRemainingTokens()));
        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}