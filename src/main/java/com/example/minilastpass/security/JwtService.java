package com.example.minilastpass.security;

import com.example.minilastpass.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtProperties properties;
    private final Key signingKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UUID userId, String email, String tokenId) {
        Instant now = Instant.now();
        Instant expiry = now.plus(properties.getExpirationMinutes(), ChronoUnit.MINUTES);
        return Jwts.builder()
            .setId(tokenId)
            .setSubject(userId.toString())
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiry))
            .addClaims(Map.of("email", email))
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception ex) {
            return false;
        }
    }

    public UUID extractUserId(String token) {
        Claims claims = parseClaims(token);
        return UUID.fromString(claims.getSubject());
    }

    public String extractEmail(String token) {
        Claims claims = parseClaims(token);
        return claims.get("email", String.class);
    }

    public String extractTokenId(String token) {
        Claims claims = parseClaims(token);
        return claims.getId();
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}
