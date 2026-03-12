package com.skill.kairo.domain.event;

import java.time.Instant;
import java.util.UUID;

public record XpAwardedEvent(
        UUID eventId,
        Instant occurredOn,
        UUID userId,
        int amountAdded,
        int newTotalXp
) implements DomainEvent {
    public XpAwardedEvent(UUID userId, int amountAdded, int newTotalXp) {
        this(UUID.randomUUID(), Instant.now(), userId, amountAdded, newTotalXp);
    }
}