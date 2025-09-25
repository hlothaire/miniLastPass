package com.example.minilastpass.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTH_COOKIE = "AUTH_TOKEN";

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final VaultUserDetailsService userDetailsService;
    private final DerivedKeyStore derivedKeyStore;

    public JwtAuthenticationFilter(JwtService jwtService, VaultUserDetailsService userDetailsService,
                                   DerivedKeyStore derivedKeyStore) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.derivedKeyStore = derivedKeyStore;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            resolveToken(request).ifPresent(token -> authenticate(request, response, token));
        }
        filterChain.doFilter(request, response);
    }

    private Optional<String> resolveToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
            .filter(cookie -> AUTH_COOKIE.equals(cookie.getName()))
            .map(Cookie::getValue)
            .findFirst();
    }

    private void authenticate(HttpServletRequest request, HttpServletResponse response, String token) {
        String tokenId = null;
        try {
            if (!jwtService.isTokenValid(token)) {
                clearAuthCookie(response);
                return;
            }
            tokenId = jwtService.extractTokenId(token);
            if (tokenId == null || tokenId.isBlank()) {
                log.debug("JWT token missing identifier");
                clearAuthCookie(response);
                return;
            }
            UUID userId = jwtService.extractUserId(token);
            byte[] vaultKey = derivedKeyStore.get(tokenId);
            if (vaultKey == null) {
                log.debug("Derived key missing for token {}", tokenId);
                derivedKeyStore.remove(tokenId);
                clearAuthCookie(response);
                return;
            }
            try {
                SecurityUser userDetails = userDetailsService.loadUserById(userId, vaultKey);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (UsernameNotFoundException ex) {
                log.debug("User {} not found for token {}", userId, tokenId);
                derivedKeyStore.remove(tokenId);
                clearAuthCookie(response);
            }
        } catch (RuntimeException ex) {
            log.debug("Failed to authenticate JWT", ex);
            if (tokenId != null) {
                derivedKeyStore.remove(tokenId);
            }
            clearAuthCookie(response);
        }
    }

    private void clearAuthCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(AUTH_COOKIE, "")
            .httpOnly(true)
            .secure(false)
            .sameSite("Lax")
            .path("/")
            .maxAge(Duration.ZERO)
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
