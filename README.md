---

# 🚦 Token Bucket Rate Limiter (Java)

A lightweight, thread-safe **Token Bucket Rate Limiter** implemented in Java.
Supports multi-user rate limiting using independent token buckets, lazy refill logic, and fine-grained locking.

This project is useful for:

* Learning rate limiting algorithms
* Practicing concurrency-safe design
* Interview preparation
* Embedding basic rate limiting into services

---

## ⭐ Features

### ✔️ Token Bucket Algorithm

Each user gets:

* `maxTokens` → bucket capacity (burst size)
* `refillRatePerSecond` → steady refill rate
* `tokens` → current available tokens

Requests are allowed **only if a token is available**.

### ✔️ Thread-Safe Design

* `ConcurrentHashMap` stores buckets per user
* Each bucket has its own `ReentrantLock`
* No global lock → excellent scalability

### ✔️ Lazy Refill

Tokens are replenished **only when a request arrives**, based on elapsed time:

```
tokens += (now - lastRefillTime) * refillRate
```

This avoids background threads and simplifies timing.

### ✔️ Logging

Useful logs emitted for:

* allowed requests
* throttled requests

(Using `java.util.logging` for simplicity)

---

## 🏗️ Architecture Overview

```
TokenBucketRateLimiter
│
├── RateLimiterConfig (global maxTokens + refillRate)
│
├── Map<String, TokenBucket> buckets     ← one bucket per user
│
└── TokenBucket (inner class)
      ├── tokens
      ├── maxTokens
      ├── refillRatePerSecond
      ├── lastRefillTimeNanos
      └── lock (ReentrantLock)
```

Every call to `allow(userId)`:

1. Fetches the user’s bucket (creates one if missing)
2. Lazily refills tokens based on elapsed time
3. If ≥1 token is available → allow & decrement
4. Else → throttle

---

## 📦 Usage Example

```java
RateLimiterConfig config = new RateLimiterConfig(5.0, 2.0); 
// maxTokens = 5, refillRate = 2 tokens/sec

TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(config);

String user = "alice";

System.out.println(limiter.allow(user)); // true
System.out.println(limiter.allow(user)); // true
System.out.println(limiter.allow(user)); // true
System.out.println(limiter.allow(user)); // true
System.out.println(limiter.allow(user)); // true

// Bucket is empty now
System.out.println(limiter.allow(user)); // false

Thread.sleep(1000);  // wait for refill

System.out.println(limiter.allow(user)); // true (refilled)
```

---

## 🧪 Testing

JUnit tests cover:

### ✔️ Burst behavior

Ensures requests beyond `maxTokens` are throttled.

### ✔️ Refill correctness

After waiting long enough, tokens should replenish.

### ✔️ User isolation

Multiple users get independent buckets.

### ✔️ Concurrency safety

20 threads hitting one user only consume up to `maxTokens`.

Example assertion:

```java
assertEquals(5, allowedCount.get());
assertEquals(15, throttledCount.get());
```

---

## 🔧 Configuration

### `RateLimiterConfig`

```java
public class RateLimiterConfig {
    private final double maxTokens;             // bucket capacity
    private final double refillRatePerSecond;   // replenishment rate
}
```

All users share the same config in this version.

---

## 🧠 Why This Implementation?

This rate limiter demonstrates several real-world concepts:

* **Fine-grained locking:**
  Per-user lock avoids global bottlenecks.

* **Lazy evaluation:**
  No background threads, refill computed on demand.

* **Correct time measurement:**
  Uses `System.nanoTime()` for monotonic elapsed time.

* **O(1) operations:**
  Every `allow()` call is constant-time.

* **Isolation:**
  One user’s behavior cannot affect another.

This mirrors how simple in-memory throttling is done inside API gateways, service meshes, and microservices.

---

## 📄 File Structure

```
src/main/java/
    org/example/service/
        RateLimiter.java
        TokenBucketRateLimiter.java
    org/example/config/
        RateLimiterConfig.java

src/test/java/
    org/example/service/
        TokenBucketRateLimiterTest.java
```

---

## 🛠️ Future Enhancements (optional ideas)

* Per-user configurable limits
* Sliding Window rate limiter
* Distributed rate limiter (Redis Lua scripts)
* Metrics: total allowed/throttled per user
* Token warm-up (gradually increasing limits)

---

## 📜 License

MIT License — free to use for learning or production experimentation.

---
