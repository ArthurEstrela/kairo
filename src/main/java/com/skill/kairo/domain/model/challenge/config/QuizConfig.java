package com.skill.kairo.domain.model.challenge.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record QuizConfig(
    String topic,
    int difficultyLevel,
    List<String> keyConcepts
) implements ChallengeConfig {

    @Override
    public String getSystemPrompt() {
        return String.format("Cria um quiz de nível %d sobre %s, focando-te nos conceitos: %s", 
            difficultyLevel, topic, String.join(", ", keyConcepts));
    }
}