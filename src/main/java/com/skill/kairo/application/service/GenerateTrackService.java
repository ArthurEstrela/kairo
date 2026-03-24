package com.skill.kairo.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skill.kairo.application.dto.response.GenerateTrackResponse;
import com.skill.kairo.application.port.AIPort;
import com.skill.kairo.application.usecase.GenerateTrackUseCase;
import com.skill.kairo.domain.exception.TrackGenerationLimitException;
import com.skill.kairo.domain.model.challenge.Challenge;
import com.skill.kairo.domain.model.challenge.config.RoleplayConfig;
import com.skill.kairo.domain.model.gamification.GamificationProfile;
import com.skill.kairo.domain.model.user.Skill;
import com.skill.kairo.domain.repository.ChallengeRepository;
import com.skill.kairo.domain.repository.GamificationRepository;
import com.skill.kairo.domain.repository.SkillRepository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GenerateTrackService implements GenerateTrackUseCase {

    private static final int MAX_CHALLENGES = 5;
    private static final int MIN_XP = 10;
    private static final int MAX_XP = 200;
    private static final int MIN_TURNS = 1;
    private static final int MAX_TURNS = 5;

    private final AIPort aiPort;
    private final SkillRepository skillRepository;
    private final ChallengeRepository challengeRepository;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;
    private final GamificationRepository gamificationRepository;

    public GenerateTrackService(AIPort aiPort, SkillRepository skillRepository,
                                ChallengeRepository challengeRepository, ObjectMapper objectMapper,
                                PlatformTransactionManager txManager,
                                GamificationRepository gamificationRepository) {
        this.aiPort = aiPort;
        this.skillRepository = skillRepository;
        this.challengeRepository = challengeRepository;
        this.objectMapper = objectMapper;
        this.transactionTemplate = new TransactionTemplate(txManager);
        this.gamificationRepository = gamificationRepository;
    }

    @Override
    public GenerateTrackResponse execute(UUID userId, String goal) {
        // 1. Verificar quota antes de chamar a IA (evita desperdício de tokens)
        GamificationProfile profile = gamificationRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Perfil de gamification não encontrado para o utilizador: " + userId));
        if (!profile.hasTrackQuota()) {
            throw new TrackGenerationLimitException();
        }
        String prompt = buildPrompt(goal);
        String rawJson;
        try {
            rawJson = aiPort.generateStructuredTrack(prompt);
        } catch (Exception e) {
            throw new RuntimeException("AI_ERROR");
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(rawJson);
        } catch (Exception e) {
            throw new RuntimeException("AI_PARSE_ERROR");
        }

        String title = root.path("title").asText("Trilha Personalizada");
        String description = root.path("description").asText("");
        JsonNode challengesNode = root.path("challenges");
        if (!challengesNode.isArray() || challengesNode.size() < 1) {
            throw new RuntimeException("AI_PARSE_ERROR");
        }

        List<JsonNode> challengeNodes = new ArrayList<>();
        challengesNode.forEach(challengeNodes::add);
        challengeNodes.sort((a, b) -> a.path("levelOrder").asInt(1) - b.path("levelOrder").asInt(1));
        if (challengeNodes.size() > MAX_CHALLENGES) {
            challengeNodes = challengeNodes.subList(0, MAX_CHALLENGES);
        }

        final List<JsonNode> finalChallengeNodes = challengeNodes;
        final String finalTitle = title;
        final String finalDescription = description;

        UUID skillId = UUID.randomUUID();
        try {
            transactionTemplate.execute(status -> {
                Skill skill = new Skill(skillId, finalTitle, finalDescription, 1, userId, false);
                skillRepository.save(skill);

                for (int i = 0; i < finalChallengeNodes.size(); i++) {
                    JsonNode cn = finalChallengeNodes.get(i);
                    int levelOrder = i + 1;
                    int xpReward = clamp(cn.path("xpReward").asInt(50), MIN_XP, MAX_XP);
                    int maxTurns = clamp(cn.path("maxTurns").asInt(2), MIN_TURNS, MAX_TURNS);
                    String cTitle = cn.path("title").asText("Desafio " + levelOrder);
                    String aiPersona = cn.path("aiPersona").asText("personagem");
                    if (aiPersona.isBlank()) aiPersona = "personagem";
                    String userObjective = cn.path("userObjective").asText("completar o desafio");
                    if (userObjective.isBlank()) userObjective = "completar o desafio";
                    String scenarioContext = cn.path("scenarioContext").asText("");
                    List<String> forbiddenWords = new ArrayList<>();
                    cn.path("forbiddenWords").forEach(fw -> forbiddenWords.add(fw.asText()));
                    RoleplayConfig config = new RoleplayConfig(aiPersona, userObjective, forbiddenWords, maxTurns, scenarioContext);
                    Challenge challenge = new Challenge(UUID.randomUUID(), skillId, cTitle, xpReward, levelOrder, config);
                    challengeRepository.save(challenge);
                }
                // Consumir 1 geração da quota (dentro da transação — rollback automático se qualquer save falhar)
                profile.consumeTrackGeneration();
                gamificationRepository.save(profile);
                return null;
            });
        } catch (RuntimeException e) {
            throw e;  // let AI_PARSE_ERROR and domain validation errors propagate as-is
        } catch (Exception e) {
            throw new RuntimeException("DB_ERROR", e);
        }

        return new GenerateTrackResponse(skillId.toString());
    }

    private String buildPrompt(String goal) {
        return """
            Gera uma trilha de aprendizagem de soft skills em formato JSON.

            Objetivo do utilizador: "%s"

            Regras:
            - Gera entre 3 a 5 desafios do tipo ROLEPLAY, de dificuldade crescente
            - maxTurns entre 1 e 5
            - xpReward entre 10 e 200, crescente
            - levelOrder começa em 1 e é consecutivo
            - scenarioContext: frase de 1-2 linhas de abertura para a IA
            - forbiddenWords: lista de palavras que o utilizador não deve usar

            Responde APENAS com JSON válido neste formato:
            {
              "title": "string",
              "description": "string",
              "challenges": [
                {
                  "title": "string",
                  "type": "ROLEPLAY",
                  "levelOrder": 1,
                  "xpReward": 50,
                  "maxTurns": 2,
                  "aiPersona": "string",
                  "userObjective": "string",
                  "scenarioContext": "string",
                  "forbiddenWords": ["string"]
                }
              ]
            }
            """.formatted(goal);
    }

    private int clamp(int value, int min, int max) {
        return Math.min(max, Math.max(min, value));
    }
}
