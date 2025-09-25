package com.example.minilastpass.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.minilastpass.security.JwtService;
import com.example.minilastpass.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Test
    void signupAndLoginFlow() {
        SignupRequest signup = new SignupRequest();
        signup.setEmail("test@example.com");
        signup.setPassword("supersecurepass");
        AuthResponse response = authService.signup(signup);
        assertThat(response.getId()).isNotNull();
        assertThat(userRepository.findByEmailIgnoreCase("test@example.com")).isPresent();

        LoginRequest login = new LoginRequest();
        login.setEmail("test@example.com");
        login.setPassword("supersecurepass");
        AuthService.LoginResult result = authService.login(login);
        assertThat(jwtService.isTokenValid(result.token())).isTrue();
        assertThat(result.profile().getEmail()).isEqualTo("test@example.com");
    }
}
