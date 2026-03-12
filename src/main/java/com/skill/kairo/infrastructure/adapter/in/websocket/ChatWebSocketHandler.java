package com.skill.kairo.infrastructure.adapter.in.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skill.kairo.application.dto.request.SubmitInteractionRequest;
import com.skill.kairo.application.dto.response.InteractionResultResponse;
import com.skill.kairo.application.usecase.EvaluateInteractionUseCase;
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
    private final ObjectMapper objectMapper;
    
    // Virtual Threads (Java 21+): Permite suportar milhões de conexões abertas com custo quase zero
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    public ChatWebSocketHandler(EvaluateInteractionUseCase evaluateInteractionUseCase, ObjectMapper objectMapper) {
        this.evaluateInteractionUseCase = evaluateInteractionUseCase;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 1. Recebemos o JSON do Next.js e transformamos no nosso DTO de entrada
        String payload = message.getPayload();
        SubmitInteractionRequest request = objectMapper.readValue(payload, SubmitInteractionRequest.class);

        // 2. Processamos tudo numa thread separada para não bloquear o servidor
        executorService.submit(() -> {
            try {
                // Simulação da resposta da IA que no futuro virá da OpenAI
                String aiResponseText = "Excelente argumento! Nota-se que dominas a negociação. Apresentaste os teus pontos de forma clara e assertiva. Vou dar-te uma pontuação elevada!";
                String[] words = aiResponseText.split(" ");

                // 3. Streaming de palavras (O efeito máquina de escrever no Next.js)
                for (String word : words) {
                    if (!session.isOpen()) break; // Se o utilizador fechou a aba, paramos
                    
                    // Enviamos um chunk JSON para o frontend processar
                    String chunkJson = String.format("{\"type\": \"CHUNK\", \"content\": \"%s \"}", word);
                    session.sendMessage(new TextMessage(chunkJson));
                    
                    Thread.sleep(150); // Delay artificial de 150ms para parecer a IA a pensar
                }

                // 4. A IA terminou de falar! Agora chamamos o nosso Maestro para dar a nota, atribuir XP e gravar
                InteractionResultResponse result = evaluateInteractionUseCase.execute(request);

                // 5. Enviamos o veredicto final (XP ganho, Vidas restantes) e fechamos o túnel
                String finalResultJson = objectMapper.writeValueAsString(result);
                if (session.isOpen()) {
                    String finalMessage = String.format("{\"type\": \"RESULT\", \"data\": %s}", finalResultJson);
                    session.sendMessage(new TextMessage(finalMessage));
                    session.close(CloseStatus.NORMAL); // Desafio concluído, conexão fechada elegantemente!
                }

            } catch (Exception e) {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage("{\"type\": \"ERROR\", \"content\": \"Falha de comunicação com a IA.\"}"));
                        session.close(CloseStatus.SERVER_ERROR);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}