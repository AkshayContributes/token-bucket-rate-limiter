package org.example.config;

public class RateLimiterConfig {
    private final double maxTokens;
    private final double refillRatePerSecond;

    public RateLimiterConfig(double maxTokens, double refillRatePerSecond) {
        if(maxTokens <= 0 || refillRatePerSecond <= 0) {
            throw new IllegalArgumentException("MaxTokens and RefillRate must be greater than 0");
        }
        this.maxTokens = maxTokens;
        this.refillRatePerSecond = refillRatePerSecond;
    }

    public double getMaxTokens() {
        return maxTokens;
    }

    public double getRefillRatePerSecond() {
        return refillRatePerSecond;
    }
}
