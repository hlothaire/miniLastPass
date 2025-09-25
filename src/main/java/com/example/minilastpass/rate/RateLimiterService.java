package com.example.minilastpass.rate;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class RateLimiterService {

    private final Map<String, Deque<Instant>> buckets = new ConcurrentHashMap<>();

    public boolean tryConsume(String key, Duration window, int maxAttempts) {
        Deque<Instant> deque = buckets.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (deque) {
            Instant cutoff = Instant.now().minus(window);
            while (!deque.isEmpty() && deque.peekFirst().isBefore(cutoff)) {
                deque.removeFirst();
            }
            if (deque.size() >= maxAttempts) {
                return false;
            }
            deque.addLast(Instant.now());
            return true;
        }
    }
}
