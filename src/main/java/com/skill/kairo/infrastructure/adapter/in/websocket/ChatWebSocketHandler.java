package com.skill.kairo.infrastructure.adapter.in.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skill.kairo.application.command.EvaluateInteractionCommand;
import com.skill.kairo.application.dto.response.InteractionResultResponse;
import com.skill.kairo.application.port.AIPort;
import com.skill.kairo.application.port.JwtPort;
import com.skill.kairo.application.usecase.EvaluateInteractionUseCase;
import com.skill.kairo.domain.repository.ChallengeRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * WebSocket handler para o roleplay com streaming real da OpenAI.
 *
 * Protocolo de mensagens:
 *   1. {"type":"AUTH","token":"eyJ..."}          → cliente autentica com JWT
 *   2. {"type":"CHALLENGE","challengeId":"...","userInput":"..."} → inicia o roleplay
 *
 * Respostas do servidor:
 *   {"type":"AUTH_OK"}                           → autenticação aceite
 *   {"type":"CHUNK","content":"..."}             → token de streaming da OpenAI
 *   {"type":"RESULT","data":{...}}               → resultado final (XP, vidas, score)
 *   {"type":"ERROR","content":"..."}             → erro, sessão encerrada
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final String SESSION_USER_ID = "userId";

    private final EvaluateInteractionUseCase evaluateInteractionUseCase;
    private final ChallengeRepository challengeRepository;
    private final AIPort aiPort;
    private final JwtPort jwtPort;
    private final ObjectMapper objectMapper;

    // Virtual Threads (Java 21): cada mensagem WebSocket corre numa thread leve
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public ChatWebSocketHandler(
            EvaluateInteractionUseCase evaluateInteractionUseCase,
            ChallengeRepository challengeRepository,
            AIPort aiPort,
            JwtPort jwtPort,
            ObjectMapper objectMapper) {
        this.evaluateInteractionUseCase = evaluateInteractionUseCase;
        this.challengeRepository = challengeRepository;
        this.aiPort = aiPort;
        this.jwtPort = jwtPort;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();

        executor.submit(() -> {
            try {
                JsonNode msg = objectMapper.readTree(payload);
                String type = msg.path("type").asText();

                switch (type) {
                    case "AUTH"      -> handleAuth(session, msg.path("token").asText());
                    case "CHALLENGE" -> handleChallenge(session, msg);
                    default          -> sendError(session, "Tipo de mensagem desconhecido: " + type);
                }
            } catch (Exception e) {
                sendError(session, e.getMessage());
            }
        });
    }

    // --- Handlers ---

    /**
     * Valida o JWT e associa o userId à sessão WebSocket.
     * Sem isto, a sessão não pode enviar mensagens CHALLENGE.
     */
    private void handleAuth(WebSocketSession session, String token) throws IOException {
        if (token == null || token.isBlank() || !jwtPort.isTokenValid(token)) {
            sendError(session, "Token inválido ou expirado.");
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        UUID userId = jwtPort.extractUserId(token);
        session.getAttributes().put(SESSION_USER_ID, userId);
        session.sendMessage(new TextMessage(
                objectMapper.writeValueAsString(new WsMessage("AUTH_OK", "Autenticado.", null))
        ));
    }

    /**
     * Executa o roleplay: obtém o system prompt, faz streaming real da OpenAI token-a-token,
     * acumula a resposta completa, avalia e devolve o resultado de gamificação.
     */
    private void handleChallenge(WebSocketSession session, JsonNode msg) throws IOException {
        UUID userId = (UUID) session.getAttributes().get(SESSION_USER_ID);
        if (userId == null) {
            sendError(session, "Não autenticado. Envie AUTH antes de CHALLENGE.");
            return;
        }

        UUID challengeId = UUID.fromString(msg.path("challengeId").asText());
        String userInput = msg.path("userInput").asText();

        String systemPrompt = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Desafio não encontrado."))
                .generatePrompt();

        // Streaming real: cada token da OpenAI vai direto para o cliente
        StringBuilder aiResponseBuilder = new StringBuilder();
        aiPort.generateStreamingResponse(systemPrompt, userInput, chunk -> {
            aiResponseBuilder.append(chunk);
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(
                            objectMapper.writeValueAsString(new WsMessage("CHUNK", chunk, null))
                    ));
                } catch (IOException ignored) {
                    // Se a sessão fechar a meio do stream, ignorar e continuar a acumular
                }
            }
        });

        // Avaliar e persistir usando a resposta REAL da IA (não hardcoded!)
        EvaluateInteractionCommand command = new EvaluateInteractionCommand(
                userId, challengeId, userInput, aiResponseBuilder.toString()
        );
        InteractionResultResponse result = evaluateInteractionUseCase.execute(command);

        if (session.isOpen()) {
            session.sendMessage(new TextMessage(
                    objectMapper.writeValueAsString(new WsMessage("RESULT", null, result))
            ));
            session.close(CloseStatus.NORMAL);
        }
    }

    // --- Utils ---

    private void sendError(WebSocketSession session, String errorMessage) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(
                        objectMapper.writeValueAsString(new WsMessage("ERROR", errorMessage, null))
                ));
                session.close(CloseStatus.SERVER_ERROR);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private record WsMessage(String type, String content, Object data) {}
}
