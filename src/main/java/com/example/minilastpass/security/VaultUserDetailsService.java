package com.example.minilastpass.security;

import com.example.minilastpass.user.UserEntity;
import com.example.minilastpass.user.UserRepository;
import java.util.UUID;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class VaultUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public VaultUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new SecurityUser(user.getId(), user.getEmail(), user.getPasswordHash(), null);
    }

    public SecurityUser loadUserById(UUID userId, byte[] vaultKey) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new SecurityUser(user.getId(), user.getEmail(), user.getPasswordHash(), vaultKey);
    }
}
