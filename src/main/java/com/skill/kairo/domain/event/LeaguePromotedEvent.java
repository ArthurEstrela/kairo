package com.skill.kairo.domain.event;

import com.skill.kairo.domain.model.gamification.LeagueTier;

import java.time.Instant;
import java.util.UUID;

public record LeaguePromotedEvent(
        UUID eventId,
        Instant occurredOn,
        UUID userId,
        LeagueTier newTier
) implements DomainEvent {
    
    // Construtor prático para o Agregado usar
    public LeaguePromotedEvent(UUID userId, LeagueTier newTier) {
        this(UUID.randomUUID(), Instant.now(), userId, newTier);
    }
}