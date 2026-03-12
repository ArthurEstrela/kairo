package com.skill.kairo.domain.model.challenge.config;

import java.util.List;

public record RoleplayConfig(
    String aiPersona,
    String userObjective,
    List<String> forbiddenWords
) implements ChallengeConfig {
    
    @Override
    public String getSystemPrompt() {
        return String.format("Ajas como %s. O objetivo do utilizador é %s. Ele não pode usar as seguintes palavras: %s", 
            aiPersona, userObjective, String.join(", ", forbiddenWords));
    }
}