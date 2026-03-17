package com.skill.kairo.application.dto.response;

import java.util.UUID;

public record SkillResponse(
        UUID id,
        String name,
        String description,
        int difficultyLevel
) {}
