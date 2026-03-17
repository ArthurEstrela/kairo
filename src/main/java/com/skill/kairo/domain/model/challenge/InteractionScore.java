package com.skill.kairo.domain.model.challenge;

public record InteractionScore(int score, String feedback) {
    public InteractionScore {
        if (score < 0 || score > 100) throw new IllegalArgumentException("Score must be 0-100");
    }
}
