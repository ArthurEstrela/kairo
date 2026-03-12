package com.skill.kairo.domain.model.challenge;

public record Score(int value) {
    public Score {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("A pontuação (Score) deve estar entre 0 e 100.");
        }
    }
}