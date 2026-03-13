package com.skill.kairo.application.dto.response;

import java.util.UUID;

public record ChallengeResponse(
        UUID id,
        UUID skillId,
        String title,
        int xpReward,
        int levelOrder,
        String type   // "ROLEPLAY" ou "QUIZ"
) {}
