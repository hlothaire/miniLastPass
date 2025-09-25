package com.example.minilastpass.vault;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class VaultItemCreateRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String username;

    private String url;

    @NotBlank
    @Size(min = 1, message = "Secret cannot be empty")
    private String secret;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
