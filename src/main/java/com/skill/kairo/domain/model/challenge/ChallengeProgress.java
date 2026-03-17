package com.skill.kairo.domain.model.challenge;

import java.time.Instant;
import java.util.UUID;

public record ChallengeProgress(
    UUID id,
    UUID userId,
    UUID challengeId,
    int bestScore,
    Instant lastUpdatedAt
) {
    public ChallengeProgress {
        if (bestScore < 0 || bestScore > 100) throw new IllegalArgumentException("bestScore must be 0–100, got: " + bestScore);
    }
}
