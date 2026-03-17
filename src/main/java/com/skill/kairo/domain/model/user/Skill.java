package com.skill.kairo.domain.model.user;

import java.util.UUID;

public record Skill(
    UUID id,
    String name,
    String description,
    int difficultyLevel,
    UUID createdByUserId,
    boolean isPublic
) {
    public Skill {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Skill name cannot be blank");
        if (difficultyLevel < 1 || difficultyLevel > 5) throw new IllegalArgumentException("Difficulty must be 1–5");
    }
}
