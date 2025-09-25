package com.example.minilastpass.vault;

import com.example.minilastpass.crypto.CryptoService;
import com.example.minilastpass.rate.RateLimiterService;
import com.example.minilastpass.security.SecurityUser;
import com.example.minilastpass.user.UserRepository;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VaultService {

    private static final Logger log = LoggerFactory.getLogger(VaultService.class);
    private static final Duration REVEAL_WINDOW = Duration.ofMinutes(5);
    private static final int REVEAL_MAX = 5;

    private final VaultItemRepository vaultItemRepository;
    private final CryptoService cryptoService;
    private final RateLimiterService rateLimiterService;
    private final UserRepository userRepository;

    public VaultService(VaultItemRepository vaultItemRepository, CryptoService cryptoService,
                        RateLimiterService rateLimiterService, UserRepository userRepository) {
        this.vaultItemRepository = vaultItemRepository;
        this.cryptoService = cryptoService;
        this.rateLimiterService = rateLimiterService;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<VaultItemView> listItems(SecurityUser user) {
        return vaultItemRepository.findAllByUser_IdOrderByCreatedAtAsc(user.getId()).stream()
            .map(item -> new VaultItemView(item.getId(), item.getTitle(), item.getUsername(), item.getUrl(),
                item.getCreatedAt(), item.getUpdatedAt()))
            .collect(Collectors.toList());
    }

    @Transactional
    public VaultItemView createItem(SecurityUser user, VaultItemCreateRequest request) {
        byte[] vaultKey = requireVaultKey(user);
        CryptoService.EncryptionResult result = encryptSecret(vaultKey, request.getSecret());
        VaultItemEntity entity = new VaultItemEntity();
        entity.setUser(userRepository.getReferenceById(user.getId()));
        entity.setTitle(request.getTitle());
        entity.setUsername(request.getUsername());
        entity.setUrl(request.getUrl());
        entity.setEncryptedSecretBase64(result.ciphertextBase64());
        entity.setNonceBase64(result.nonceBase64());
        VaultItemEntity saved = vaultItemRepository.save(entity);
        return new VaultItemView(saved.getId(), saved.getTitle(), saved.getUsername(), saved.getUrl(),
            saved.getCreatedAt(), saved.getUpdatedAt());
    }

    @Transactional
    public VaultItemView updateItem(SecurityUser user, UUID itemId, VaultItemUpdateRequest request) {
        VaultItemEntity item = vaultItemRepository.findByIdAndUser_Id(itemId, user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
        if (request.getTitle() != null) {
            item.setTitle(request.getTitle());
        }
        if (request.getUsername() != null) {
            item.setUsername(request.getUsername());
        }
        if (request.getUrl() != null) {
            item.setUrl(request.getUrl());
        }
        if (request.getSecret() != null) {
            byte[] vaultKey = requireVaultKey(user);
            CryptoService.EncryptionResult result = encryptSecret(vaultKey, request.getSecret());
            item.setEncryptedSecretBase64(result.ciphertextBase64());
            item.setNonceBase64(result.nonceBase64());
        }
        VaultItemEntity saved = vaultItemRepository.save(item);
        return new VaultItemView(saved.getId(), saved.getTitle(), saved.getUsername(), saved.getUrl(),
            saved.getCreatedAt(), saved.getUpdatedAt());
    }

    @Transactional
    public void deleteItem(SecurityUser user, UUID itemId) {
        VaultItemEntity item = vaultItemRepository.findByIdAndUser_Id(itemId, user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
        vaultItemRepository.delete(item);
    }

    @Transactional(readOnly = true)
    public RevealResponse revealSecret(SecurityUser user, UUID itemId, String ipAddress) {
        if (!rateLimiterService.tryConsume("reveal:" + user.getId(), REVEAL_WINDOW, REVEAL_MAX)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many reveal attempts");
        }
        VaultItemEntity item = vaultItemRepository.findByIdAndUser_Id(itemId, user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
        byte[] vaultKey = requireVaultKey(user);
        String secret = decryptSecret(vaultKey, item.getEncryptedSecretBase64(), item.getNonceBase64());
        log.info("Secret revealed user={} item={} ip={}", user.getId(), item.getId(), ipAddress);
        return new RevealResponse(secret);
    }

    private byte[] requireVaultKey(SecurityUser user) {
        byte[] key = user.getVaultKey();
        if (key == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vault key unavailable");
        }
        return key;
    }

    private CryptoService.EncryptionResult encryptSecret(byte[] vaultKey, String secret) {
        try {
            return cryptoService.encrypt(vaultKey, secret);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vault key invalid", ex);
        }
    }

    private String decryptSecret(byte[] vaultKey, String ciphertextBase64, String nonceBase64) {
        try {
            return cryptoService.decrypt(vaultKey, ciphertextBase64, nonceBase64);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vault key invalid", ex);
        }
    }
}
