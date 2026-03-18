package com.skill.kairo.infrastructure.adapter.out.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skill.kairo.application.port.AIPort;
import com.skill.kairo.domain.model.challenge.InteractionScore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "openai")
public class OpenAIAdapter implements AIPort {

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4o-mini";

    private final String apiKey;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    // HttpClient reutilizável (thread-safe, partilha o connection pool)
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public OpenAIAdapter(
            @Value("${openai.api.key}") String apiKey,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(OPENAI_API_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public String generateResponse(String systemPrompt, String userInput) {
        String body = buildRequestBody(systemPrompt, userInput, false);
        String rawResponse = restClient.post()
                .body(body)
                .retrieve()
                .body(String.class);
        return extractContent(rawResponse);
    }

    /**
     * Streaming real via SSE da OpenAI usando Java HttpClient (sem dependências extra).
     * Cada token recebido é entregue imediatamente ao consumer para reencaminhar ao WebSocket.
     * Bloqueia até o stream terminar — seguro em Virtual Threads (Java 21).
     */
    @Override
    public void generateStreamingResponse(String systemPrompt, String userInput, Consumer<String> chunkConsumer) {
        try {
            String body = buildRequestBody(systemPrompt, userInput, true);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OPENAI_API_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            httpClient.send(request, HttpResponse.BodyHandlers.ofLines())
                    .body()
                    .filter(line -> line.startsWith("data: ") && !line.equals("data: [DONE]"))
                    .forEach(line -> {
                        try {
                            JsonNode node = objectMapper.readTree(line.substring(6));
                            String content = node.path("choices").path(0)
                                    .path("delta").path("content").asText("");
                            if (!content.isEmpty()) {
                                chunkConsumer.accept(content);
                            }
                        } catch (Exception ignored) {
                            // Linha malformada: ignorar e continuar o stream
                        }
                    });

        } catch (Exception e) {
            throw new RuntimeException("Erro no streaming da OpenAI", e);
        }
    }

    // -----------------------------------------------------------------------
    // NOT IMPLEMENTED — track generation is Gemini-only for now.
    // If ai.provider=openai is set, calling either method below will throw
    // UnsupportedOperationException at runtime. Do NOT add openai as a
    // provider for track generation until these are properly implemented.
    // -----------------------------------------------------------------------

    @Override
    public String generateStructuredTrack(String prompt) {
        throw new UnsupportedOperationException("OpenAI structured track generation not implemented");
    }

    @Override
    public InteractionScore evaluateInteraction(String systemPrompt, List<String> conversationHistory) {
        throw new UnsupportedOperationException("OpenAI multi-turn evaluation not implemented");
    }

    private String buildRequestBody(String systemPrompt, String userMessage, boolean stream) {
        try {
            Map<String, Object> request = Map.of(
                    "model", MODEL,
                    "stream", stream,
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

    private String extractContent(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            return root.path("choices").path(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar resposta da OpenAI", e);
        }
    }
}
