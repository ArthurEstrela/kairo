package com.skill.kairo.domain.model.gamification;

import com.skill.kairo.domain.event.DomainEvent;
import com.skill.kairo.domain.event.LeaguePromotedEvent;
import com.skill.kairo.domain.event.LifeLostEvent;
import com.skill.kairo.domain.event.XpAwardedEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class GamificationProfile {

    private final UUID id;
    private final UUID userId;

    private ExperiencePoints xp;
    private Lives lives;
    private Streak streak;
    private LeagueTier tier;
    private Instant lastLifeLostAt;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // Limiares de XP para promoção de liga
    private static final int XP_SILVER   = 500;
    private static final int XP_GOLD     = 1_500;
    private static final int XP_PLATINUM = 3_500;
    private static final int XP_DIAMOND  = 7_500;

    // Construtor para novo perfil (onboarding)
    public GamificationProfile(UUID id, UUID userId) {
        this.id = id;
        this.userId = userId;
        this.xp = new ExperiencePoints(0);
        this.lives = new Lives(5, 5);
        this.streak = new Streak(0);
        this.tier = LeagueTier.BRONZE;
        this.lastLifeLostAt = null;
    }

    // Construtor de reconstrução (usado pelo Repository ao buscar do banco)
    public GamificationProfile(UUID id, UUID userId, int currentXp, int currentLives, int maxLives,
                                int currentStreak, LeagueTier tier, Instant lastLifeLostAt) {
        this.id = id;
        this.userId = userId;
        this.xp = new ExperiencePoints(currentXp);
        this.lives = new Lives(currentLives, maxLives);
        this.streak = new Streak(currentStreak);
        this.tier = tier;
        this.lastLifeLostAt = lastLifeLostAt;
    }

    // --- COMPORTAMENTOS DE NEGÓCIO ---

    public void awardXp(int amount) {
        this.xp = this.xp.add(amount);
        registerEvent(new XpAwardedEvent(this.userId, amount, this.xp.value()));
        checkLeaguePromotion();
    }

    public void failChallenge() {
        this.lives = this.lives.decrement();
        this.streak = this.streak.reset();
        this.lastLifeLostAt = Instant.now();
        registerEvent(new LifeLostEvent(this.userId, this.lives.current()));
    }

    public void maintainStreak() {
        this.streak = this.streak.increment();
    }

    /**
     * Restaura uma vida. Chamado pelo scheduler a cada 4 horas.
     * Devolve true se uma vida foi restaurada.
     */
    public boolean restoreOneLife() {
        if (lives.current() >= lives.max()) return false;
        this.lives = new Lives(lives.current() + 1, lives.max());
        if (lives.current() >= lives.max()) {
            this.lastLifeLostAt = null;
        }
        return true;
    }

    /**
     * Restaura todas as vidas imediatamente (upgrade para Premium).
     */
    public void restoreLives() {
        this.lives = this.lives.restoreFull();
        this.lastLifeLostAt = null;
    }

    private void checkLeaguePromotion() {
        LeagueTier newTier = calculateTier(this.xp.value());
        if (newTier.ordinal() > this.tier.ordinal()) {
            this.tier = newTier;
            registerEvent(new LeaguePromotedEvent(this.userId, this.tier));
        }
    }

    private LeagueTier calculateTier(int totalXp) {
        if (totalXp >= XP_DIAMOND)  return LeagueTier.DIAMOND;
        if (totalXp >= XP_PLATINUM) return LeagueTier.PLATINUM;
        if (totalXp >= XP_GOLD)     return LeagueTier.GOLD;
        if (totalXp >= XP_SILVER)   return LeagueTier.SILVER;
        return LeagueTier.BRONZE;
    }

    // --- CONTROLE DE EVENTOS (DDD PURO) ---

    private void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return Collections.unmodifiableList(events);
    }

    // --- GETTERS ---

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public int getCurrentXp() { return xp.value(); }
    public int getCurrentLives() { return lives.current(); }
    public int getMaxLives() { return lives.max(); }
    public int getCurrentStreak() { return streak.count(); }
    public LeagueTier getTier() { return tier; }
    public Instant getLastLifeLostAt() { return lastLifeLostAt; }
}
