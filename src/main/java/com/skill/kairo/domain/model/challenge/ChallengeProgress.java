package com.skill.kairo.domain.model.challenge;

import java.time.Instant;
import java.util.UUID;

public record ChallengeProgress(
    UUID id,
    UUID userId,
    UUID challengeId,
    int bestScore,
    Instant lastUpdatedAt
) {}
