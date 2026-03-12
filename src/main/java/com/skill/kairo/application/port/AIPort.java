package com.skill.kairo.application.port;

import com.skill.kairo.domain.model.challenge.Score;

public interface AIPort {
    // Envia o contexto do desafio e a resposta do utilizador, e devolve a nota (Score)
    Score evaluateInteraction(String systemPrompt, String userInput);
}