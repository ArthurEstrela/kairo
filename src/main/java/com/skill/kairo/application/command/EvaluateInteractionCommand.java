package com.skill.kairo.application.command;

import java.util.UUID;

/**
 * Comando imutável que carrega tudo o que o use case precisa para avaliar uma interação.
 * aiResponse pode ser null quando chamado via REST: o service gera-o internamente.
 * Quando chamado via WebSocket, aiResponse já vem preenchido (foi streamado ao cliente).
 */
public record EvaluateInteractionCommand(
        UUID userId,
        UUID challengeId,
        String userInput,
        String aiResponse
) {}
