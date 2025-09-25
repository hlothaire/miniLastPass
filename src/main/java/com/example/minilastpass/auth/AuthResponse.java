package com.example.minilastpass.auth;

import java.util.UUID;

public class AuthResponse {

    private UUID id;
    private String email;

    public AuthResponse(UUID id, String email) {
        this.id = id;
        this.email = email;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }
}
