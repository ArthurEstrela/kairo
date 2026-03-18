package com.skill.kairo.infrastructure.adapter.in.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skill.kairo.application.port.AIPort;
import com.skill.kairo.application.port.JwtPort;
import com.skill.kairo.domain.model.challenge.Challenge;
import com.skill.kairo.domain.model.challenge.ChallengeProgress;
import com.skill.kairo.domain.model.challenge.Interaction;
import com.skill.kairo.domain.model.challenge.InteractionScore;
import com.skill.kairo.domain.model.challenge.Score;
import com.skill.kairo.domain.model.challenge.config.RoleplayConfig;
import com.skill.kairo.domain.model.user.Skill;
import com.skill.kairo.domain.repository.*;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Multi-turn WebSocket roleplay handler.
 * Protocol: AUTH → INIT → (opening CHUNK stream) → INIT_ACK → TURN → TURN_ACK → CHUNK → RESULT
 * Server closes WebSocket after every ERROR and after RESULT.
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final String KEY_USER_ID = "userId";
    private static final String KEY_SESSION  = "roleplaySession";

    private final JwtPort jwtPort;
    private final AIPort aiPort;
    private final SkillRepository skillRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeProgressRepository progressRepository;
    private final GamificationRepository gamificationRepository;
    private final InteractionRepository interactionRepository;
    private final ObjectMapper objectMapper;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public ChatWebSocketHandler(
            JwtPort jwtPort, AIPort aiPort,
            SkillRepository skillRepository,
            ChallengeRepository challengeRepository,
            ChallengeProgressRepository progressRepository,
            GamificationRepository gamificationRepository,
            InteractionRepository interactionRepository,
            ObjectMapper objectMapper) {
        this.jwtPort = jwtPort;
        this.aiPort = aiPort;
        this.skillRepository = skillRepository;
        this.challengeRepository = challengeRepository;
        this.progressRepository = progressRepository;
        this.gamificationRepository = gamificationRepository;
        this.interactionRepository = interactionRepository;
        this.objectMapper = objectMapper;
    }

    private static class RoleplaySession {
        UUID challengeId;
        int maxTurns;
        int currentTurn = 0;
        List<String> history = new ArrayList<>();
        boolean openingComplete = false;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        executor.submit(() -> {
            try {
                JsonNode msg = objectMapper.readTree(payload);
                String type = msg.path("type").asText();
                switch (type) {
                    case "AUTH" -> handleAuth(session, msg.path("token").asText());
                    case "INIT" -> handleInit(session, msg.path("challengeId").asText());
                    case "TURN" -> handleTurn(session, msg.path("userInput").asText());
                    default     -> sendError(session, "UNKNOWN_TYPE");
                }
            } catch (Exception e) {
                sendError(session, "INTERNAL_ERROR");
            }
        });
    }

    private void handleAuth(WebSocketSession session, String token) throws IOException {
        if (token == null || token.isBlank() || !jwtPort.isTokenValid(token)) {
            sendErrorAndClose(session, "UNAUTHORIZED");
            return;
        }
        session.getAttributes().put(KEY_USER_ID, jwtPort.extractUserId(token));
    }

    private void handleInit(WebSocketSession session, String challengeIdStr) throws IOException {
        UUID userId = (UUID) session.getAttributes().get(KEY_USER_ID);
        if (userId == null) { sendErrorAndClose(session, "UNAUTHORIZED"); return; }

        if (session.getAttributes().containsKey(KEY_SESSION)) {
            sendErrorAndClose(session, "SESSION_ALREADY_ACTIVE"); return;
        }

        UUID challengeId;
        try { challengeId = UUID.fromString(challengeIdStr); }
        catch (Exception e) { sendErrorAndClose(session, "CHALLENGE_NOT_FOUND"); return; }

        Challenge challenge = challengeRepository.findById(challengeId).orElse(null);
        if (challenge == null || !(challenge.getConfig() instanceof RoleplayConfig)) {
            sendErrorAndClose(session, "CHALLENGE_NOT_FOUND"); return;
        }

        Skill track = skillRepository.findById(challenge.getSkillId()).orElse(null);
        if (track != null && track.createdByUserId() != null
                && !track.createdByUserId().equals(userId) && !track.isPublic()) {
            sendErrorAndClose(session, "CHALLENGE_FORBIDDEN"); return;
        }

        String status = computeStatus(userId, challenge, challengeRepository.findBySkillId(challenge.getSkillId()));
        if ("LOCKED".equals(status)) {
            sendErrorAndClose(session, "CHALLENGE_LOCKED"); return;
        }

        int lives = gamificationRepository.getLives(userId);
        if (lives <= 0) { sendErrorAndClose(session, "NO_LIVES"); return; }

        RoleplayConfig config = (RoleplayConfig) challenge.getConfig();
        RoleplaySession rs = new RoleplaySession();
        rs.challengeId = challengeId;
        rs.maxTurns = config.maxTurns();
        session.getAttributes().put(KEY_SESSION, rs);

        StringBuilder opening = new StringBuilder();
        String openingPrompt = config.getSystemPrompt();
        String openingUserMsg = config.scenarioContext() != null && !config.scenarioContext().isBlank()
            ? config.scenarioContext()
            : "Inicia o roleplay.";
        try {
            aiPort.generateStreamingResponse(openingPrompt, openingUserMsg, chunk -> {
                opening.append(chunk);
                send(session, Map.of("type", "CHUNK", "delta", chunk));
            });
        } catch (Exception e) {
            session.getAttributes().remove(KEY_SESSION);
            sendErrorAndClose(session, "OPENING_FAILED"); return;
        }

        rs.history.add(opening.toString());
        rs.openingComplete = true;
        send(session, Map.of("type", "INIT_ACK", "maxTurns", rs.maxTurns));
    }

    private void handleTurn(WebSocketSession session, String userInput) throws IOException {
        UUID userId = (UUID) session.getAttributes().get(KEY_USER_ID);
        if (userId == null) { sendErrorAndClose(session, "UNAUTHORIZED"); return; }

        RoleplaySession rs = (RoleplaySession) session.getAttributes().get(KEY_SESSION);
        if (rs == null || !rs.openingComplete) {
            sendErrorAndClose(session, "SESSION_NOT_READY"); return;
        }

        rs.currentTurn++;
        send(session, Map.of("type", "TURN_ACK", "turn", rs.currentTurn, "maxTurns", rs.maxTurns));
        rs.history.add(userInput);

        Challenge challenge = challengeRepository.findById(rs.challengeId).orElseThrow();
        RoleplayConfig config = (RoleplayConfig) challenge.getConfig();

        String contextualInput = buildContextualInput(rs.history, userInput);

        StringBuilder aiReply = new StringBuilder();
        try {
            aiPort.generateStreamingResponse(config.getSystemPrompt(), contextualInput, chunk -> {
                aiReply.append(chunk);
                send(session, Map.of("type", "CHUNK", "delta", chunk));
            });
        } catch (Exception e) {
            session.getAttributes().remove(KEY_SESSION);
            sendErrorAndClose(session, "TURN_STREAM_FAILED"); return;
        }
        rs.history.add(aiReply.toString());

        if (rs.currentTurn >= rs.maxTurns) {
            finalizeTurn(session, userId, challenge, config, rs);
        }
    }

    private void finalizeTurn(WebSocketSession session, UUID userId,
                               Challenge challenge, RoleplayConfig config, RoleplaySession rs) throws IOException {
        String evalPrompt = buildEvalPrompt(config);

        InteractionScore interactionScore;
        try {
            interactionScore = aiPort.evaluateInteraction(evalPrompt, rs.history);
        } catch (Exception e) {
            sendErrorAndClose(session, "EVALUATION_FAILED"); return;
        }

        int score = interactionScore.score();

        int prevBestScore = progressRepository
            .findByUserIdAndChallengeId(userId, rs.challengeId)
            .map(ChallengeProgress::bestScore).orElse(0);

        int livesRemaining;
        if (score < 60) {
            livesRemaining = gamificationRepository.deductLifeAndReturn(userId);
        } else {
            livesRemaining = gamificationRepository.getLives(userId);
        }

        UUID interactionId = UUID.randomUUID();
        String lastUserTurn = "";
        for (int i = rs.history.size() - 1; i >= 0; i--) {
            if (i % 2 == 1) { lastUserTurn = rs.history.get(i); break; }
        }
        String historyJson;
        try {
            historyJson = objectMapper.writeValueAsString(rs.history);
        } catch (Exception e) {
            historyJson = "[]";
        }
        interactionRepository.save(new Interaction(
            interactionId, userId, rs.challengeId, lastUserTurn, historyJson, new Score(score)
        ));

        int xpAwarded = (score >= 70 && prevBestScore < 70) ? challenge.getXpReward() : 0;
        gamificationRepository.findByUserId(userId).ifPresent(profile -> {
            profile.awardXp(xpAwarded);
            gamificationRepository.save(profile);
        });

        send(session, Map.of(
            "type", "RESULT",
            "interactionId", interactionId.toString(),
            "score", score,
            "xpAwarded", xpAwarded,
            "livesRemaining", livesRemaining
        ));

        if (session.isOpen()) {
            session.close(CloseStatus.NORMAL);
        }
    }

    private String buildContextualInput(List<String> history, String latestUserInput) {
        if (history.size() <= 1) {
            return latestUserInput;
        }
        StringBuilder sb = new StringBuilder("Contexto da conversa até agora:\n");
        for (int i = 0; i < history.size() - 1; i++) {
            sb.append(i % 2 == 0 ? "IA: " : "Utilizador: ").append(history.get(i)).append("\n");
        }
        sb.append("\nResposta actual do utilizador: ").append(latestUserInput);
        return sb.toString();
    }

    private String computeStatus(UUID userId, Challenge target, List<Challenge> allChallenges) {
        List<Challenge> sorted = allChallenges.stream()
            .sorted(Comparator.comparingInt(Challenge::getLevelOrder)).toList();

        int prevBest = 100;
        for (int i = 0; i < sorted.size(); i++) {
            Challenge c = sorted.get(i);
            int best = progressRepository
                .findByUserIdAndChallengeId(userId, c.getId())
                .map(ChallengeProgress::bestScore).orElse(0);

            String status;
            if (i == 0) {
                status = best >= 60 ? "COMPLETED" : "ACTIVE";
            } else {
                status = prevBest >= 60 ? (best >= 60 ? "COMPLETED" : "ACTIVE") : "LOCKED";
            }
            prevBest = best;

            if (c.getId().equals(target.getId())) return status;
        }
        return "LOCKED";
    }

    private String buildEvalPrompt(RoleplayConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("Avalia a conversa onde a IA interpretou '").append(config.aiPersona())
          .append("' e o objetivo do utilizador era '").append(config.userObjective()).append("'. ");
        sb.append("Pontua de 0 a 100 baseado em cumprimento do objetivo e qualidade de comunicação");
        if (!config.forbiddenWords().isEmpty()) {
            sb.append(", e ausência das palavras proibidas: ")
              .append(String.join(", ", config.forbiddenWords()));
        }
        sb.append(". Responde em JSON: {\"score\": <0-100>, \"feedback\": \"<1 frase>\"}.");
        return sb.toString();
    }

    private void send(WebSocketSession session, Map<String, Object> data) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(data)));
            }
        } catch (Exception ignored) {}
    }

    private void sendError(WebSocketSession session, String code) {
        send(session, Map.of("type", "ERROR", "code", code));
    }

    private void sendErrorAndClose(WebSocketSession session, String code) {
        sendError(session, code);
        try { if (session.isOpen()) session.close(CloseStatus.POLICY_VIOLATION); }
        catch (Exception ignored) {}
    }
}
