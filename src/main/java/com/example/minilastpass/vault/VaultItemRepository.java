package com.example.minilastpass.vault;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VaultItemRepository extends JpaRepository<VaultItemEntity, UUID> {
    List<VaultItemEntity> findAllByUser_IdOrderByCreatedAtAsc(UUID userId);
    Optional<VaultItemEntity> findByIdAndUser_Id(UUID id, UUID userId);
}
