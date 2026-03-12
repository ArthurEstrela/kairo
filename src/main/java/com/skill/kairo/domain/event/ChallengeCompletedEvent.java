package com.skill.kairo.domain.event;

import java.time.Instant;
import java.util.UUID;

public record ChallengeCompletedEvent(
        UUID eventId,
        Instant occurredOn,
        UUID userId,
        UUID challengeId,
        int score
) implements DomainEvent {
    
    // Construtor prático para o Agregado usar
    public ChallengeCompletedEvent(UUID userId, UUID challengeId, int score) {
        this(UUID.randomUUID(), Instant.now(), userId, challengeId, score);
    }
}