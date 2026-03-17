package com.skill.kairo.domain.repository;

import com.skill.kairo.domain.model.gamification.GamificationProfile;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GamificationRepository {
    Optional<GamificationProfile> findByUserId(UUID userId);
    void save(GamificationProfile profile);

    /**
     * Devolve todos os perfis com vidas em falta cuja última perda foi antes do threshold.
     * Usado pelo scheduler de recarga de vidas.
     */
    List<GamificationProfile> findAllWithLivesLostBefore(Instant threshold);

    int deductLifeAndReturn(UUID userId);

    int getLives(UUID userId);
}
