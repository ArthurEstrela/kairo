package com.skill.kairo.domain.model.challenge.config;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class RoleplayConfigTest {
    @Test
    void shouldIncludeScenarioContextInSystemPrompt() {
        var config = new RoleplayConfig("chefe", "pedir aumento", List.of(), 2, "Reunião de avaliação anual");
        assertThat(config.getSystemPrompt()).contains("Reunião de avaliação anual");
    }

    @Test
    void shouldOmitForbiddenWordsClauseWhenEmpty() {
        var config = new RoleplayConfig("chefe", "pedir aumento", List.of(), 2, "contexto");
        assertThat(config.getSystemPrompt()).doesNotContain("Proibidas");
        assertThat(config.getSystemPrompt()).doesNotContain("proibidas");
    }

    @Test
    void factoryMethodHandlesMissingMaxTurnsAndScenarioContext() {
        // Simulate legacy JSON deserialization (no maxTurns, no scenarioContext)
        var config = RoleplayConfig.of("persona", "objective", List.of("word"), null, null);
        assertThat(config.maxTurns()).isEqualTo(0);
        assertThat(config.scenarioContext()).isEqualTo("");
    }
}
