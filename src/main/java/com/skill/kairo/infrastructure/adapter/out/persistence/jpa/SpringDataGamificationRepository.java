package com.skill.kairo.infrastructure.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataGamificationRepository extends JpaRepository<GamificationProfileEntity, UUID> {
    
    Optional<GamificationProfileEntity> findByUserId(UUID userId);
    
}