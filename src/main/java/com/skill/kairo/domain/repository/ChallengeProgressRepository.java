package com.skill.kairo.domain.repository;

import com.skill.kairo.domain.model.challenge.ChallengeProgress;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChallengeProgressRepository {
    Optional<ChallengeProgress> findByUserIdAndChallengeId(UUID userId, UUID challengeId);
    List<ChallengeProgress> findByUserIdAndChallengeIdIn(UUID userId, List<UUID> challengeIds);
    void upsert(UUID userId, UUID challengeId, int bestScore);
}
