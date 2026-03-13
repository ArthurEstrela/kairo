package com.skill.kairo.infrastructure.adapter.in.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skill.kairo.application.dto.request.SubmitInteractionRequest;
import com.skill.kairo.application.dto.response.InteractionResultResponse;
import com.skill.kairo.application.port.AIPort;
import com.skill.kairo.application.usecase.EvaluateInteractionUseCase;
import com.skill.kairo.domain.repository.ChallengeRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final EvaluateInteractionUseCase evaluateInteractionUseCase;
    private final ChallengeRepository challengeRepository;
    private final AIPort aiPort;
    private final ObjectMapper objectMapper;

    // Virtual Threads (Java 21): suporta milhares de conexões simultâneas com custo mínimo
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public ChatWebSocketHandler(
            EvaluateInteractionUseCase evaluateInteractionUseCase,
            ChallengeRepository challengeRepository,
            AIPort aiPort,
            ObjectMapper objectMapper) {
        this.evaluateInteractionUseCase = evaluateInteractionUseCase;
        this.challengeRepository = challengeRepository;
        this.aiPort = aiPort;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();

        executor.submit(() -> {
            try {
                SubmitInteractionRequest request = objectMapper.readValue(payload, SubmitInteractionRequest.class);

                // 1. Obter o system prompt do desafio
                String systemPrompt = challengeRepository.findById(request.challengeId())
                        .orElseThrow(() -> new IllegalArgumentException("Desafio não encontrado."))
                        .generatePrompt();

                // 2. Chamar a OpenAI para obter a resposta da persona (conteúdo real)
                String aiResponseText = aiPort.generateResponse(systemPrompt, request.userInput());

                // 3. Streaming palavra-a-palavra → efeito máquina de escrever no frontend
                String[] words = aiResponseText.split(" ");
                for (String word : words) {
                    if (!session.isOpen()) return;
                    String chunk = objectMapper.writeValueAsString(
                            new WsMessage("CHUNK", word + " ", null)
                    );
                    session.sendMessage(new TextMessage(chunk));
                    Thread.sleep(60); // 60ms → ritmo natural de leitura
                }

                // 4. Maestro avalia, atribui XP e grava tudo
                InteractionResultResponse result = evaluateInteractionUseCase.execute(request);

                // 5. Veredicto final → o frontend sabe se ganhou XP ou perdeu uma vida
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(
                            objectMapper.writeValueAsString(new WsMessage("RESULT", null, result))
                    ));
                    session.close(CloseStatus.NORMAL);
                }

            } catch (Exception e) {
                sendError(session, e.getMessage());
            }
        });
    }

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
