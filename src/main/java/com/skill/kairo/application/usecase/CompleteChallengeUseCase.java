package com.skill.kairo.application.usecase;

import java.util.UUID;

public interface CompleteChallengeUseCase {
    void execute(UUID userId, UUID challengeId, UUID interactionId);
}
