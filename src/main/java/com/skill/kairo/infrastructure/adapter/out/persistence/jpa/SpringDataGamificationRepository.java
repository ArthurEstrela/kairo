package com.skill.kairo.infrastructure.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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

    @Query("SELECT p FROM GamificationProfileEntity p " +
           "WHERE p.quotaResetDate <= :now AND p.availableTrackGenerations IS NOT NULL")
    List<GamificationProfileEntity> findAllWithExpiredQuota(@Param("now") Instant now);

    @Modifying
    @Transactional
    @Query(value = "UPDATE gamification_profiles SET current_lives = GREATEST(current_lives - 1, 0), last_life_lost_at = NOW() WHERE user_id = :userId", nativeQuery = true)
    void deductLife(@Param("userId") UUID userId);

    @Query("SELECT g.currentLives FROM GamificationProfileEntity g WHERE g.userId = :userId")
    Optional<Integer> getLivesByUserId(@Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE gamification_profiles SET current_lives = GREATEST(current_lives - 1, 0), last_life_lost_at = NOW() WHERE user_id = :userId RETURNING current_lives", nativeQuery = true)
    int deductLifeAndReturn(@Param("userId") UUID userId);
}
