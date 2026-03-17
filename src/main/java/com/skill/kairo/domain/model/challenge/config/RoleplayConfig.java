package com.skill.kairo.domain.model.challenge.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record RoleplayConfig(
    String aiPersona,
    String userObjective,
    List<String> forbiddenWords,
    int maxTurns,
    String scenarioContext
) implements ChallengeConfig {

    @JsonCreator
    public static RoleplayConfig of(
        @JsonProperty("aiPersona") String aiPersona,
        @JsonProperty("userObjective") String userObjective,
        @JsonProperty("forbiddenWords") List<String> forbiddenWords,
        @JsonProperty("maxTurns") Integer maxTurns,
        @JsonProperty("scenarioContext") String scenarioContext
    ) {
        return new RoleplayConfig(
            aiPersona,
            userObjective,
            forbiddenWords != null ? forbiddenWords : List.of(),
            maxTurns != null ? maxTurns : 0,
            scenarioContext != null ? scenarioContext : ""
        );
    }

    @Override
    public String getSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("Ajas como ").append(aiPersona).append(". ");
        sb.append("O objetivo do utilizador é ").append(userObjective).append(". ");
        if (scenarioContext != null && !scenarioContext.isBlank()) {
            sb.append("Contexto: ").append(scenarioContext).append(". ");
        }
        if (forbiddenWords != null && !forbiddenWords.isEmpty()) {
            sb.append("Palavras proibidas: ").append(String.join(", ", forbiddenWords)).append(".");
        }
        return sb.toString();
    }
}
