package com.example.minilastpass.vault;

import java.time.Instant;
import java.util.UUID;

public class VaultItemView {

    private UUID id;
    private String title;
    private String username;
    private String url;
    private Instant createdAt;
    private Instant updatedAt;

    public VaultItemView(UUID id, String title, String username, String url, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.title = title;
        this.username = username;
        this.url = url;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getUsername() {
        return username;
    }

    public String getUrl() {
        return url;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
