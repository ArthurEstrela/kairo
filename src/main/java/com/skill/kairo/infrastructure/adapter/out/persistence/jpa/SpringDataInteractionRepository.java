package com.skill.kairo.infrastructure.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SpringDataInteractionRepository extends JpaRepository<InteractionEntity, UUID> {
    
    // O Spring gera automaticamente a query SQL para buscar o histórico de um utilizador!
    List<InteractionEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
}