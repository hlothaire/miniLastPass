package com.example.minilastpass.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotEmpty;

@Validated
@ConfigurationProperties(prefix = "application.security.cors")
public class CorsProperties {

    @NotEmpty
    private List<String> allowedOrigins;

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }
}
