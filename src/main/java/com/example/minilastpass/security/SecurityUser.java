package com.example.minilastpass.security;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUser implements UserDetails {

    private final UUID id;
    private final String email;
    private final String passwordHash;
    private final byte[] vaultKey;

    public SecurityUser(UUID id, String email, String passwordHash, byte[] vaultKey) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.vaultKey = vaultKey;
    }

    public UUID getId() {
        return id;
    }

    public byte[] getVaultKey() {
        return vaultKey;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
