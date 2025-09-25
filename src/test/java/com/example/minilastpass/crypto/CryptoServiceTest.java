package com.example.minilastpass.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class CryptoServiceTest {

    private final CryptoService cryptoService = new CryptoService();

    @Test
    void encryptThenDecryptRoundTrip() {
        char[] password = "averysecurepassword".toCharArray();
        byte[] salt = "saltysalt1234567".getBytes(StandardCharsets.UTF_8);
        byte[] key = cryptoService.deriveKey(password, salt);
        CryptoService.EncryptionResult result = cryptoService.encrypt(key, "secret-value");
        String decrypted = cryptoService.decrypt(key, result.ciphertextBase64(), result.nonceBase64());
        assertThat(decrypted).isEqualTo("secret-value");
    }
}
