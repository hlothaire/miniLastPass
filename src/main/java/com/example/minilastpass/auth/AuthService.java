package com.example.minilastpass.auth;

import com.example.minilastpass.crypto.CryptoService;
import com.example.minilastpass.rate.RateLimiterService;
import com.example.minilastpass.security.DerivedKeyStore;
import com.example.minilastpass.security.JwtService;
import com.example.minilastpass.user.UserEntity;
import com.example.minilastpass.user.UserRepository;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2Factory.Argon2Types;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final Duration LOGIN_WINDOW = Duration.ofMinutes(1);
    private static final int LOGIN_MAX_ATTEMPTS = 10;

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final DerivedKeyStore derivedKeyStore;
    private final CryptoService cryptoService;
    private final RateLimiterService rateLimiterService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(UserRepository userRepository, JwtService jwtService, DerivedKeyStore derivedKeyStore,
                       CryptoService cryptoService, RateLimiterService rateLimiterService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.derivedKeyStore = derivedKeyStore;
        this.cryptoService = cryptoService;
        this.rateLimiterService = rateLimiterService;
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        byte[] kdfSalt = new byte[16];
        secureRandom.nextBytes(kdfSalt);
        String passwordHash = hashPassword(request.getPassword());
        UserEntity user = new UserEntity();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordHash);
        user.setKdfSaltBase64(Base64.getEncoder().encodeToString(kdfSalt));
        UserEntity saved = userRepository.save(user);
        return new AuthResponse(saved.getId(), saved.getEmail());
    }

    @Transactional(readOnly = true)
    public LoginResult login(LoginRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase();
        if (!rateLimiterService.tryConsume("login:" + normalizedEmail, LOGIN_WINDOW, LOGIN_MAX_ATTEMPTS)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many login attempts");
        }
        UserEntity user = userRepository.findByEmailIgnoreCase(normalizedEmail)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        char[] passwordChars = request.getPassword().toCharArray();
        char[] passwordCopy = passwordChars.clone();
        boolean verified;
        Argon2 argon2 = Argon2Factory.create(Argon2Types.ARGON2id);
        try {
            verified = argon2.verify(user.getPasswordHash(), passwordChars);
        } finally {
            argon2.wipeArray(passwordChars);
        }
        if (!verified) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        byte[] derivedKey;
        try {
            derivedKey = cryptoService.deriveKey(passwordCopy, Base64.getDecoder().decode(user.getKdfSaltBase64()));
        } finally {
            Arrays.fill(passwordCopy, '\0');
        }
        String tokenId = UUID.randomUUID().toString();
        derivedKeyStore.put(tokenId, derivedKey);
        String token = jwtService.generateToken(user.getId(), user.getEmail(), tokenId);
        return new LoginResult(token, tokenId, new AuthResponse(user.getId(), user.getEmail()));
    }

    public void logout(String tokenId) {
        if (tokenId != null) {
            derivedKeyStore.remove(tokenId);
        }
    }

    private String hashPassword(String password) {
        Argon2 argon2 = Argon2Factory.create(Argon2Types.ARGON2id);
        char[] passwordChars = password.toCharArray();
        try {
            return argon2.hash(4, CryptoService.KDF_MEMORY_KB, CryptoService.KDF_PARALLELISM, passwordChars);
        } finally {
            argon2.wipeArray(passwordChars);
        }
    }

    public record LoginResult(String token, String tokenId, AuthResponse profile) { }
}
