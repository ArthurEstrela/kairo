package com.skill.kairo.domain.model.gamification;

import com.skill.kairo.domain.event.DomainEvent;
import com.skill.kairo.domain.event.LifeLostEvent;
import com.skill.kairo.domain.event.XpAwardedEvent;

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

    // Lista onde o Agregado anota as mudanças de estado
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // Construtor para novo perfil
    public GamificationProfile(UUID id, UUID userId) {
        this.id = id;
        this.userId = userId;
        this.xp = new ExperiencePoints(0);
        this.lives = new Lives(5, 5);
        this.streak = new Streak(0);
        this.tier = LeagueTier.BRONZE;
    }

    // Construtor de reconstrução (usado pelo Repository ao buscar do banco)
    public GamificationProfile(UUID id, UUID userId, int currentXp, int currentLives, int maxLives, int currentStreak, LeagueTier tier) {
        this.id = id;
        this.userId = userId;
        this.xp = new ExperiencePoints(currentXp);
        this.lives = new Lives(currentLives, maxLives);
        this.streak = new Streak(currentStreak);
        this.tier = tier;
    }

    // --- COMPORTAMENTOS DE NEGÓCIO ---

    public void awardXp(int amount) {
        this.xp = this.xp.add(amount);
        registerEvent(new XpAwardedEvent(this.userId, amount, this.xp.value()));
        
        // Exemplo: lógica interna do agregado
        checkLeaguePromotion();
    }

    public void failChallenge() {
        this.lives = this.lives.decrement();
        this.streak = this.streak.reset(); // Perdeu o desafio, zera a ofensiva do dia atual
        registerEvent(new LifeLostEvent(this.userId, this.lives.current()));
    }

    public void maintainStreak() {
        this.streak = this.streak.increment();
    }

    private void checkLeaguePromotion() {
        // No futuro, se xp > 1000, this.tier = LeagueTier.SILVER e registra LeaguePromotedEvent
    }

    // --- CONTROLE DE EVENTOS (DDD PURO) ---

    private void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear(); // Limpa a lista após extrair para evitar dupla publicação
        return Collections.unmodifiableList(events);
    }

    // --- GETTERS (Sem setters para manter a imutabilidade da entidade) ---
    
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public int getCurrentXp() { return xp.value(); }
    public int getCurrentLives() { return lives.current(); }
    public int getMaxLives() { return lives.max(); }
    public int getCurrentStreak() { return streak.count(); }
    public LeagueTier getTier() { return tier; }
}