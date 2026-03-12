package com.skill.kairo.domain.model.gamification;

import java.time.Instant;
import java.util.UUID;

public class League {

    private final UUID id;
    private final LeagueTier tier;
    private final Instant seasonStart;
    private final Instant seasonEnd;

    public League(UUID id, LeagueTier tier, Instant seasonStart, Instant seasonEnd) {
        if (tier == null) {
            throw new IllegalArgumentException("A Liga precisa ter um Tier (nível).");
        }
        if (seasonStart == null || seasonEnd == null) {
            throw new IllegalArgumentException("As datas de início e fim da temporada são obrigatórias.");
        }
        if (seasonEnd.isBefore(seasonStart)) {
            throw new IllegalArgumentException("A data de término da temporada não pode ser anterior à data de início.");
        }
        
        this.id = id;
        this.tier = tier;
        this.seasonStart = seasonStart;
        this.seasonEnd = seasonEnd;
    }

    // Comportamento rico: O domínio sabe responder se a liga está rolando agora
    public boolean isActive(Instant now) {
        return !now.isBefore(seasonStart) && now.isBefore(seasonEnd);
    }

    // --- GETTERS ---
    public UUID getId() { return id; }
    public LeagueTier getTier() { return tier; }
    public Instant getSeasonStart() { return seasonStart; }
    public Instant getSeasonEnd() { return seasonEnd; }
}