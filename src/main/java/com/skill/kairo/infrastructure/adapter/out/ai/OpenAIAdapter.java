package com.skill.kairo.infrastructure.adapter.out.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skill.kairo.application.port.AIPort;
import com.skill.kairo.domain.model.challenge.Score;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class OpenAIAdapter implements AIPort {

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4o-mini";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public OpenAIAdapter(
            @Value("${openai.api.key}") String apiKey,
            ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(OPENAI_API_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public String generateResponse(String systemPrompt, String userInput) {
        String body = buildRequestBody(systemPrompt, userInput);
        String rawResponse = restClient.post()
                .body(body)
                .retrieve()
                .body(String.class);
        return extractContent(rawResponse);
    }

    @Override
    public Score evaluateInteraction(String systemPrompt, String userInput) {
        String evaluatorPrompt = buildEvaluatorPrompt(systemPrompt, userInput);
        String body = buildRequestBody(evaluatorPrompt, "Avalia a resposta acima.");
        String rawResponse = restClient.post()
                .body(body)
                .retrieve()
                .body(String.class);

        String content = extractContent(rawResponse);
        return new Score(parseScore(content));
    }

    private String buildRequestBody(String systemPrompt, String userMessage) {
        try {
            Map<String, Object> request = Map.of(
                    "model", MODEL,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userMessage)
                    )
            );
            return objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao construir request para OpenAI", e);
        }
    }

    private String buildEvaluatorPrompt(String challengeSystemPrompt, String userInput) {
        return """
                És um avaliador rigoroso de soft skills. O utilizador participou num desafio de roleplay.

                Contexto do desafio:
                %s

                A resposta do utilizador foi:
                "%s"

                Avalia a qualidade desta resposta numa escala de 0 a 100 com base em:
                - Clareza e persuasão da argumentação
                - Cumprimento do objetivo do desafio
                - Uso de técnicas adequadas de comunicação/negociação
                - Evitação das palavras proibidas (se aplicável)

                Responde APENAS com um número inteiro de 0 a 100. Sem texto adicional.
                """.formatted(challengeSystemPrompt, userInput);
    }

    private String extractContent(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            return root.path("choices").path(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar resposta da OpenAI", e);
        }
    }

    private int parseScore(String content) {
        try {
            String cleaned = content.trim().replaceAll("[^0-9]", "");
            if (cleaned.isEmpty()) return 50;
            int score = Integer.parseInt(cleaned);
            return Math.min(100, Math.max(0, score));
        } catch (NumberFormatException e) {
            return 50;
        }
    }
}
