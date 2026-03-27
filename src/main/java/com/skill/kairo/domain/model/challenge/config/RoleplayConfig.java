package com.skill.kairo.domain.model.challenge.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RoleplayConfig(
    String aiPersona,
    String userObjective,
    List<String> forbiddenWords,
    int maxTurns,
    String scenarioContext
) implements ChallengeConfig {

    public RoleplayConfig {
        if (aiPersona == null || aiPersona.isBlank()) throw new IllegalArgumentException("aiPersona cannot be blank");
        if (userObjective == null || userObjective.isBlank()) throw new IllegalArgumentException("userObjective cannot be blank");
        forbiddenWords = forbiddenWords != null ? forbiddenWords : List.of();
        scenarioContext = scenarioContext != null ? scenarioContext : "";
    }

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
        String prompt = "Aja como " + aiPersona + ". " +
                "O objetivo do utilizador é " + userObjective + ". " +
                (!scenarioContext.isBlank() ? "Contexto: " + scenarioContext + ". " : "") +
                (!forbiddenWords.isEmpty() ? "Palavras proibidas: " + String.join(", ", forbiddenWords) + "." : "");
        return prompt;
    }
}
