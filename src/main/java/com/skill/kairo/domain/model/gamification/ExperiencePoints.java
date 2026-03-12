package com.skill.kairo.domain.model.gamification;

public record ExperiencePoints(int value) {
    public ExperiencePoints {
        if (value < 0) {
            throw new IllegalArgumentException("XP não pode ser negativo.");
        }
    }

    public ExperiencePoints add(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Quantidade a adicionar deve ser maior que zero.");
        }
        return new ExperiencePoints(this.value + amount);
    }
}