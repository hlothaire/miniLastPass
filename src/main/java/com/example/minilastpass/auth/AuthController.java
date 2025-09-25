package com.example.minilastpass.auth;

import com.example.minilastpass.config.JwtProperties;
import com.example.minilastpass.security.JwtAuthenticationFilter;
import com.example.minilastpass.security.JwtService;
import com.example.minilastpass.security.SecurityUser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtProperties jwtProperties;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtProperties jwtProperties, JwtService jwtService) {
        this.authService = authService;
        this.jwtProperties = jwtProperties;
        this.jwtService = jwtService;
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me(@AuthenticationPrincipal SecurityUser user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return ResponseEntity.ok(new AuthResponse(user.getId(), user.getUsername()));
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        AuthResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthService.LoginResult result = authService.login(request);
        ResponseCookie cookie = ResponseCookie.from(JwtAuthenticationFilter.AUTH_COOKIE, result.token())
            .httpOnly(true)
            .secure(false)
            .sameSite("Lax")
            .maxAge(Duration.ofMinutes(jwtProperties.getExpirationMinutes()))
            .path("/")
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(result.profile());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        Optional<String> token = resolveToken(request);
        token.ifPresent(value -> {
            try {
                authService.logout(jwtService.extractTokenId(value));
            } catch (Exception ignored) {
            }
        });
        ResponseCookie cookie = ResponseCookie.from(JwtAuthenticationFilter.AUTH_COOKIE, "")
            .httpOnly(true)
            .secure(false)
            .sameSite("Lax")
            .maxAge(Duration.ZERO)
            .path("/")
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.noContent().build();
    }

    private Optional<String> resolveToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
            .filter(cookie -> JwtAuthenticationFilter.AUTH_COOKIE.equals(cookie.getName()))
            .map(Cookie::getValue)
            .findFirst();
    }
}
