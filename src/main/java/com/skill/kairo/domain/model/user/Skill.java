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
        if (difficultyLevel < 0 || difficultyLevel > 4) throw new IllegalArgumentException("Difficulty must be 0–4");
    }
}
