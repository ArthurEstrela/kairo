package com.skill.kairo.application.dto.request;

import java.util.UUID;

public record SubmitInteractionRequest(
    UUID userId,
    UUID challengeId,
    String userInput
) {}