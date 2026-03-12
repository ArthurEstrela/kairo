package com.skill.kairo.application.dto.response;

public record InteractionResultResponse(
    int scoreObtained,
    int totalXp,
    int livesRemaining,
    String feedbackMessage // Pode ser expandido no futuro
) {}