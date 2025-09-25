package com.example.minilastpass.vault;

public class RevealResponse {

    private String secret;

    public RevealResponse(String secret) {
        this.secret = secret;
    }

    public String getSecret() {
        return secret;
    }
}
