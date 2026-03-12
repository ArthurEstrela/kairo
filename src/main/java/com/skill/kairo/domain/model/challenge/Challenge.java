package com.skill.kairo.domain.model.challenge;

import com.skill.kairo.domain.model.challenge.config.ChallengeConfig;
import java.util.UUID;

public class Challenge {

    private final UUID id;
    private final UUID skillId;
    private final String title;
    private final int xpReward;
    private final int levelOrder;
    
    // O nosso Value Object tipado a brilhar!
    private final ChallengeConfig config;

    public Challenge(UUID id, UUID skillId, String title, int xpReward, int levelOrder, ChallengeConfig config) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("O título do desafio não pode estar vazio.");
        }
        if (xpReward <= 0) {
            throw new IllegalArgumentException("A recompensa de XP deve ser maior que zero.");
        }
        if (config == null) {
            throw new IllegalArgumentException("A configuração do desafio é obrigatória.");
        }
        
        this.id = id;
        this.skillId = skillId;
        this.title = title;
        this.xpReward = xpReward;
        this.levelOrder = levelOrder;
        this.config = config;
    }

    // O Agregado fornece o prompt exato, mas não faz a chamada de rede.
    public String generatePrompt() {
        return config.getSystemPrompt();
    }

    // --- GETTERS ---
    public UUID getId() { return id; }
    public UUID getSkillId() { return skillId; }
    public String getTitle() { return title; }
    public int getXpReward() { return xpReward; }
    public int getLevelOrder() { return levelOrder; }
    public ChallengeConfig getConfig() { return config; }
}