package com.example.minilastpass.vault;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.minilastpass.auth.AuthService;
import com.example.minilastpass.auth.LoginRequest;
import com.example.minilastpass.auth.SignupRequest;
import com.example.minilastpass.crypto.CryptoService;
import com.example.minilastpass.security.SecurityUser;
import com.example.minilastpass.user.UserEntity;
import com.example.minilastpass.user.UserRepository;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class VaultServiceTest {

    @Autowired
    private VaultService vaultService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CryptoService cryptoService;

    private SecurityUser user;
    private final String password = "supersecurepass";

    @BeforeEach
    void setupUser() {
        if (user == null) {
            SignupRequest signupRequest = new SignupRequest();
            signupRequest.setEmail("vault@example.com");
            signupRequest.setPassword(password);
            authService.signup(signupRequest);
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setEmail("vault@example.com");
            loginRequest.setPassword(password);
            authService.login(loginRequest);
            UserEntity entity = userRepository.findByEmailIgnoreCase("vault@example.com").orElseThrow();
            byte[] salt = Base64.getDecoder().decode(entity.getKdfSaltBase64());
            byte[] key = cryptoService.deriveKey(password.toCharArray(), salt);
            user = new SecurityUser(entity.getId(), entity.getEmail(), entity.getPasswordHash(), key);
        }
    }

    @Test
    void createUpdateRevealFlow() {
        VaultItemCreateRequest createRequest = new VaultItemCreateRequest();
        createRequest.setTitle("Example");
        createRequest.setUsername("user1");
        createRequest.setUrl("https://example.com");
        createRequest.setSecret("plaintext");
        VaultItemView created = vaultService.createItem(user, createRequest);
        assertThat(created.getId()).isNotNull();

        VaultItemUpdateRequest updateRequest = new VaultItemUpdateRequest();
        updateRequest.setUsername("user2");
        updateRequest.setSecret("new-secret");
        VaultItemView updated = vaultService.updateItem(user, created.getId(), updateRequest);
        assertThat(updated.getUsername()).isEqualTo("user2");

        RevealResponse reveal = vaultService.revealSecret(user, created.getId(), "127.0.0.1");
        assertThat(reveal.getSecret()).isEqualTo("new-secret");

        vaultService.deleteItem(user, created.getId());
        List<VaultItemView> items = vaultService.listItems(user);
        assertThat(items).isEmpty();
    }
}
