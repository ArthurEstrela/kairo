package com.skill.kairo.infrastructure.adapter.out.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skill.kairo.application.port.AIPort;
import com.skill.kairo.domain.model.challenge.InteractionScore;
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
    public String generateStructuredTrack(String prompt) {
        String url = BASE_URL + ":generateContent?key=" + apiKey;
        try {
            Map<String, Object> body = Map.of(
                "contents", List.of(
                    Map.of("role", "user", "parts", List.of(Map.of("text", prompt)))
                ),
                "generationConfig", Map.of(
                    "temperature", 0.7,
                    "maxOutputTokens", 2048,
                    "responseMimeType", "application/json"
                )
            );
            String requestBody = objectMapper.writeValueAsString(body);
            HttpResponse<String> response = send(url, requestBody);
            String text = extractText(response.body());
            if (text == null || text.isBlank()) {
                throw new RuntimeException("Gemini returned empty JSON for track generation");
            }
            return text;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar trilha estruturada: " + e.getMessage(), e);
        }
    }

    @Override
    public InteractionScore evaluateInteraction(String systemPrompt, List<String> conversationHistory) {
        String historyText = buildHistoryText(conversationHistory);
        String evaluatorPrompt = """
            És um avaliador rigoroso de soft skills. O utilizador participou num roleplay multi-turno.

            Sistema do desafio:
            %s

            Conversa completa (alternando: IA, utilizador, IA, utilizador, ...):
            %s

            Avalia o desempenho do utilizador de 0 a 100 baseado em:
            - Cumprimento do objetivo
            - Qualidade da comunicação
            - Ausência de palavras proibidas (se aplicável)

            Responde APENAS com JSON: {"score": <0-100>, "feedback": "<1 frase>"}
            """.formatted(systemPrompt, historyText);

        String url = BASE_URL + ":generateContent?key=" + apiKey;
        String body = buildRequestBody(evaluatorPrompt, "Avalia a conversa acima.");
        HttpResponse<String> response = send(url, body);
        String content = extractText(response.body());
        return parseInteractionScore(content);
    }

    private String buildHistoryText(List<String> history) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < history.size(); i++) {
            sb.append(i % 2 == 0 ? "IA: " : "Utilizador: ").append(history.get(i)).append("\n");
        }
        return sb.toString();
    }

    private InteractionScore parseInteractionScore(String content) {
        try {
            JsonNode root = objectMapper.readTree(content);
            int score = Math.min(100, Math.max(0, root.path("score").asInt(50)));
            String feedback = root.path("feedback").asText("");
            return new InteractionScore(score, feedback.isBlank() ? "Sem feedback" : feedback);
        } catch (Exception e) {
            return new InteractionScore(50, "Sem feedback");
        }
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

}
