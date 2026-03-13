package com.skill.kairo.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SubmitInteractionRequest(
    @NotNull UUID challengeId,
    @NotBlank String userInput
) {}