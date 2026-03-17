package com.skill.kairo.infrastructure.adapter.out.persistence.jpa;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataInteractionRepository extends JpaRepository<InteractionEntity, UUID> {

    List<InteractionEntity> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    List<InteractionEntity> findByUserIdAndCreatedAtAfter(UUID userId, Instant since);

    long countByUserId(UUID userId);

    Optional<InteractionEntity> findByIdAndUserIdAndChallengeId(UUID id, UUID userId, UUID challengeId);
}