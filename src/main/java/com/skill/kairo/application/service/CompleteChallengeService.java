package com.skill.kairo.application.service;

import com.skill.kairo.application.usecase.CompleteChallengeUseCase;
import com.skill.kairo.domain.repository.ChallengeProgressRepository;
import com.skill.kairo.domain.repository.InteractionRepository;

import java.util.NoSuchElementException;
import java.util.UUID;

public class CompleteChallengeService implements CompleteChallengeUseCase {

    private final InteractionRepository interactionRepository;
    private final ChallengeProgressRepository progressRepository;

    public CompleteChallengeService(InteractionRepository interactionRepository,
                                    ChallengeProgressRepository progressRepository) {
        this.interactionRepository = interactionRepository;
        this.progressRepository = progressRepository;
    }

    @Override
    public void execute(UUID userId, UUID challengeId, UUID interactionId) {
        var interaction = interactionRepository
            .findByIdAndUserIdAndChallengeId(interactionId, userId, challengeId)
            .orElseThrow(() -> new NoSuchElementException("INTERACTION_NOT_FOUND"));

        progressRepository.upsert(userId, challengeId, interaction.getScore().value());
    }
}
