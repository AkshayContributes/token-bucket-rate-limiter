package org.example.service;

import org.example.config.RateLimiterConfig;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class TokenBucketRateLimiter implements RateLimiter{



    private final RateLimiterConfig config;
    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private static final Logger logger = Logger.getLogger("org.example.service.TokenBucketRateLimiter");

    public TokenBucketRateLimiter(RateLimiterConfig config) {
        this.config = config;
    }

    @Override
    public boolean allow(String userId) {
        TokenBucket bucket = buckets.computeIfAbsent(
                userId,
                id -> new TokenBucket(config.getMaxTokens(), config.getRefillRatePerSecond())
        );

        boolean allowed = bucket.tryConsume();

        if (allowed) {
            logger.info(String.format("Request allowed for user ---%s---", userId ));
        }else {
            logger.info(String.format("Request not allowed for user ---%s---", userId ));
        }

        return allowed;
    }

    private static class TokenBucket {
        private final double maxTokens;
        private final double refillRatePerSecond;

        private double tokens;
        private long lastRefillTimeNanos;

        private final ReentrantLock lock = new ReentrantLock();

        public TokenBucket(double maxTokens, double refillRatePerSecond) {
            this.maxTokens = maxTokens;
            this.refillRatePerSecond = refillRatePerSecond;
            this.tokens = maxTokens;
            this.lastRefillTimeNanos = System.nanoTime();
        }

        boolean tryConsume() {
            lock.lock();
            try {
                long now = System.nanoTime();
                double elapsedSeconds = (now - this.lastRefillTimeNanos) / 1_000_000_000.0;

                if (elapsedSeconds > 0) {
                    double tokensToAdd = elapsedSeconds * this.refillRatePerSecond;
                    this.tokens = Math.min(this.tokens + tokensToAdd, this.maxTokens);
                    this.lastRefillTimeNanos = now;
                }

                if (tokens >= 1.0) {
                    tokens -= 1.0;
                    return true;
                }else {
                    return false;
                }
            }finally {
                lock.unlock();
            }
        }
    }

}
