package com.example.minilastpass.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class DerivedKeyStore {

    private static final Duration MAX_LIFETIME = Duration.ofHours(4);

    private final Map<String, Entry> keys = new ConcurrentHashMap<>();

    public void put(String tokenId, byte[] key) {
        Objects.requireNonNull(tokenId, "tokenId");
        Objects.requireNonNull(key, "key");
        keys.put(tokenId, new Entry(key.clone(), Instant.now()));
    }

    public byte[] get(String tokenId) {
        if (tokenId == null) {
            return null;
        }
        Entry entry = keys.get(tokenId);
        if (entry == null) {
            return null;
        }
        if (Instant.now().isAfter(entry.createdAt().plus(MAX_LIFETIME))) {
            keys.remove(tokenId);
            return null;
        }
        return entry.key().clone();
    }

    public void remove(String tokenId) {
        if (tokenId != null) {
            keys.remove(tokenId);
        }
    }

    private record Entry(byte[] key, Instant createdAt) { }
}
