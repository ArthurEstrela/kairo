package com.skill.kairo.domain.model.challenge.config;

public sealed interface ChallengeConfig permits RoleplayConfig, QuizConfig {
    String getSystemPrompt();
}