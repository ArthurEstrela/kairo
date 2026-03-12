package com.skill.kairo.domain.model.gamification;

public record Streak(int count) {
    public Streak {
        if (count < 0) {
            throw new IllegalArgumentException("A ofensiva (streak) não pode ser negativa.");
        }
    }

    public Streak increment() {
        return new Streak(this.count + 1);
    }

    public Streak reset() {
        return new Streak(0);
    }
}