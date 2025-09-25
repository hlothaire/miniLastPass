package com.example.minilastpass.crypto;

import de.mkammerer.argon2.Argon2Advanced;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2Factory.Argon2Types;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class CryptoService {

    public static final int KDF_ITERATIONS = 3;
    public static final int KDF_MEMORY_KB = 1 << 16; // 64 MB
    public static final int KDF_PARALLELISM = 1;
    public static final int KEY_LENGTH = 32;

    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_NONCE_LENGTH = 12;

    private final SecureRandom secureRandom = new SecureRandom();

    public byte[] deriveKey(char[] password, byte[] salt) {
        Argon2Advanced argon2 = Argon2Factory.createAdvanced(Argon2Types.ARGON2id, salt.length, KEY_LENGTH);
        try {
            return argon2.pbkdf(KDF_ITERATIONS, KDF_MEMORY_KB, KDF_PARALLELISM, password, StandardCharsets.UTF_8, salt, KEY_LENGTH);
        } finally {
            argon2.wipeArray(password);
        }
    }

    public EncryptionResult encrypt(byte[] key, String plaintext) {
        try {
            byte[] nonce = new byte[GCM_NONCE_LENGTH];
            secureRandom.nextBytes(nonce);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKey secretKey = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, nonce));
            byte[] cipherBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return new EncryptionResult(
                Base64.getEncoder().encodeToString(cipherBytes),
                Base64.getEncoder().encodeToString(nonce));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to encrypt secret", e);
        }
    }

    public String decrypt(byte[] key, String ciphertextBase64, String nonceBase64) {
        try {
            byte[] ciphertext = Base64.getDecoder().decode(ciphertextBase64);
            byte[] nonce = Base64.getDecoder().decode(nonceBase64);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKey secretKey = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, nonce));
            byte[] plainBytes = cipher.doFinal(ciphertext);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to decrypt secret", e);
        }
    }

    public record EncryptionResult(String ciphertextBase64, String nonceBase64) {}
}
