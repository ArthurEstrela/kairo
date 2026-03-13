package com.skill.kairo.infrastructure.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataGamificationRepository extends JpaRepository<GamificationProfileEntity, UUID> {

    Optional<GamificationProfileEntity> findByUserId(UUID userId);

    @Query("""
        SELECT g FROM GamificationProfileEntity g
        WHERE g.currentLives < g.maxLives
          AND g.lastLifeLostAt IS NOT NULL
          AND g.lastLifeLostAt <= :threshold
        """)
    List<GamificationProfileEntity> findAllWithLivesLostBefore(@Param("threshold") Instant threshold);
}
