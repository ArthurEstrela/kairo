package com.skill.kairo.infrastructure.adapter.out.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skill.kairo.application.port.AIPort;
import com.skill.kairo.domain.model.challenge.Score;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Adapter para a API do Google Gemini (AI Studio).
 *
 * Activado por padrão — basta definir gemini.api.key em application.properties.
 * Para voltar à OpenAI, define ai.provider=openai.
 *
 * Modelo gratuito: gemini-2.0-flash
 * Documentação: https://ai.google.dev/api/generate-content
 */
@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "gemini", matchIfMissing = true)
public class GeminiAdapter implements AIPort {

    private static final String BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash";

    private final String apiKey;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public GeminiAdapter(
            @Value("${gemini.api.key}") String apiKey,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
    }

    // ─── AIPort ──────────────────────────────────────────────────────────────

    @Override
    public String generateResponse(String systemPrompt, String userInput) {
        String url  = BASE_URL + ":generateContent?key=" + apiKey;
        String body = buildRequestBody(systemPrompt, userInput);

        HttpResponse<String> response = send(url, body);
        return extractText(response.body());
    }

    /**
     * Streaming via SSE do Gemini.
     * Cada chunk chega como "data: {...}" e é entregue imediatamente ao consumer.
     * Bloqueia até o stream terminar — seguro em Virtual Threads (Java 21).
     */
    @Override
    public void generateStreamingResponse(String systemPrompt, String userInput, Consumer<String> chunkConsumer) {
        String url  = BASE_URL + ":streamGenerateContent?alt=sse&key=" + apiKey;
        String body = buildRequestBody(systemPrompt, userInput);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            httpClient.send(request, HttpResponse.BodyHandlers.ofLines())
                    .body()
                    .filter(line -> line.startsWith("data: "))
                    .forEach(line -> {
                        try {
                            String json    = line.substring(6).trim();
                            String content = extractText(json);
                            if (!content.isEmpty()) {
                                chunkConsumer.accept(content);
                            }
                        } catch (Exception ignored) {
                            // Linha malformada: ignorar e continuar o stream
                        }
                    });

        } catch (Exception e) {
            throw new RuntimeException("Erro no streaming do Gemini", e);
        }
    }

    @Override
    public Score evaluateInteraction(String systemPrompt, String userInput) {
        String evaluatorPrompt = buildEvaluatorPrompt(systemPrompt, userInput);
        String url  = BASE_URL + ":generateContent?key=" + apiKey;
        String body = buildRequestBody(evaluatorPrompt, "Avalia a resposta acima.");

        HttpResponse<String> response = send(url, body);
        String content = extractText(response.body());
        return new Score(parseScore(content));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Monta o payload no formato Gemini:
     * system_instruction + contents[{role: user, parts: [{text}]}]
     */
    private String buildRequestBody(String systemPrompt, String userInput) {
        try {
            Map<String, Object> body = Map.of(
                    "system_instruction", Map.of(
                            "parts", List.of(Map.of("text", systemPrompt))
                    ),
                    "contents", List.of(
                            Map.of(
                                    "role", "user",
                                    "parts", List.of(Map.of("text", userInput))
                            )
                    ),
                    "generationConfig", Map.of(
                            "temperature", 0.7,
                            "maxOutputTokens", 1024
                    )
            );
            return objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao construir request para Gemini", e);
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

    /**
     * Extrai o texto do campo candidates[0].content.parts[0].text
     */
    private String extractText(String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            return root.path("candidates").path(0)
                    .path("content").path("parts").path(0)
                    .path("text").asText("");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar resposta do Gemini: " + rawJson, e);
        }
    }

    private HttpResponse<String> send(String url, String body) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException("Erro na chamada ao Gemini", e);
        }
    }

    private int parseScore(String content) {
        try {
            String cleaned = content.trim().replaceAll("[^0-9]", "");
            if (cleaned.isEmpty()) return 50;
            return Math.min(100, Math.max(0, Integer.parseInt(cleaned)));
        } catch (NumberFormatException e) {
            return 50;
        }
    }
}
