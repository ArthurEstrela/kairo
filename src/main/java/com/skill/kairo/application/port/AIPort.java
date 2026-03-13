package com.skill.kairo.application.port;

import com.skill.kairo.domain.model.challenge.Score;

import java.util.function.Consumer;

public interface AIPort {
    /**
     * A IA assume a persona do desafio e responde ao utilizador em personagem.
     * Chamada síncrona — usada pelo endpoint REST (sem streaming).
     */
    String generateResponse(String systemPrompt, String userInput);

    /**
     * Streaming real via SSE da OpenAI: cada token/chunk é entregue ao consumer.
     * A chamada bloqueia até a OpenAI terminar o stream (seguro em Virtual Threads).
     * A resposta completa é a concatenação de todos os chunks recebidos.
     */
    void generateStreamingResponse(String systemPrompt, String userInput, Consumer<String> chunkConsumer);

    /**
     * A IA avalia a qualidade da resposta do utilizador e devolve uma nota 0-100.
     * Usado após o roleplay para determinar XP ganho ou vida perdida.
     */
    Score evaluateInteraction(String systemPrompt, String userInput);
}
