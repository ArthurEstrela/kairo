package com.skill.kairo.infrastructure.adapter.out.persistence.jpa;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataInteractionRepository extends JpaRepository<InteractionEntity, UUID> {

    List<InteractionEntity> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}