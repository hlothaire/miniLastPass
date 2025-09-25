package com.example.minilastpass.vault;

import com.example.minilastpass.security.SecurityUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/vault")
public class VaultController {

    private final VaultService vaultService;

    public VaultController(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    @GetMapping
    public List<VaultItemView> list(@AuthenticationPrincipal SecurityUser user) {
        return vaultService.listItems(requireUser(user));
    }

    @PostMapping
    public ResponseEntity<VaultItemView> create(@AuthenticationPrincipal SecurityUser user,
                                                @Valid @RequestBody VaultItemCreateRequest request) {
        VaultItemView view = vaultService.createItem(requireUser(user), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(view);
    }

    @PutMapping("/{id}")
    public VaultItemView update(@AuthenticationPrincipal SecurityUser user, @PathVariable("id") UUID id,
                                @Valid @RequestBody VaultItemUpdateRequest request) {
        return vaultService.updateItem(requireUser(user), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal SecurityUser user, @PathVariable("id") UUID id) {
        vaultService.deleteItem(requireUser(user), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/reveal")
    public RevealResponse reveal(@AuthenticationPrincipal SecurityUser user, @PathVariable("id") UUID id,
                                 HttpServletRequest request) {
        return vaultService.revealSecret(requireUser(user), id, request.getRemoteAddr());
    }

    private SecurityUser requireUser(SecurityUser user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return user;
    }
}
