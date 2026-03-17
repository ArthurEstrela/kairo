package com.skill.kairo.application.dto.response;

public record TrackChallengeResponse(
        String id,
        String title,
        int xpReward,
        int levelOrder,
        int maxTurns,
        String status,
        int bestScore
) {}
