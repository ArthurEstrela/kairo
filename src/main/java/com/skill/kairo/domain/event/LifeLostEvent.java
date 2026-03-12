package com.skill.kairo.domain.event;

import java.time.Instant;
import java.util.UUID;

public record LifeLostEvent(
        UUID eventId,
        Instant occurredOn,
        UUID userId,
        int livesRemaining
) implements DomainEvent {
    public LifeLostEvent(UUID userId, int livesRemaining) {
        this(UUID.randomUUID(), Instant.now(), userId, livesRemaining);
    }
}