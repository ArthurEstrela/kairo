package com.skill.kairo.application.port;

import com.skill.kairo.domain.model.challenge.Score;

public interface AIPort {
    /**
     * A IA assume a persona do desafio e responde ao utilizador em personagem.
     * Usado pelo WebSocket para o efeito de streaming.
     */
    String generateResponse(String systemPrompt, String userInput);

    /**
     * A IA avalia a qualidade da resposta do utilizador e devolve uma nota 0-100.
     * Usado após o roleplay para determinar XP ganho ou vida perdida.
     */
    Score evaluateInteraction(String systemPrompt, String userInput);
}
