package com.skill.kairo.infrastructure.adapter.out.persistence.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skill.kairo.domain.exception.InvalidChallengeConfigException;
import com.skill.kairo.domain.model.challenge.Challenge;
import com.skill.kairo.domain.model.challenge.config.ChallengeConfig;
import com.skill.kairo.domain.model.challenge.config.QuizConfig;
import com.skill.kairo.domain.model.challenge.config.RoleplayConfig;
import com.skill.kairo.infrastructure.adapter.out.persistence.jpa.ChallengeEntity;
import org.springframework.stereotype.Component;

@Component
public class ChallengeMapper {

    private final ObjectMapper objectMapper;

    public ChallengeMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Challenge toDomain(ChallengeEntity entity) {
        ChallengeConfig config = deserializeConfig(entity.getConfigType(), entity.getConfigData());
        return new Challenge(
                entity.getId(),
                entity.getSkillId(),
                entity.getTitle(),
                entity.getXpReward(),
                entity.getLevelOrder(),
                config
        );
    }

    public ChallengeEntity toEntity(Challenge domain) {
        try {
            String configType = switch (domain.getConfig()) {
                case RoleplayConfig ignored -> "ROLEPLAY";
                case QuizConfig ignored -> "QUIZ";
            };
            String configData = objectMapper.writeValueAsString(domain.getConfig());
            return new ChallengeEntity(
                    domain.getId(),
                    domain.getSkillId(),
                    domain.getTitle(),
                    domain.getXpReward(),
                    domain.getLevelOrder(),
                    configType,
                    configData
            );
        } catch (Exception e) {
            throw new InvalidChallengeConfigException("Erro ao serializar config do desafio: " + e.getMessage());
        }
    }

    private ChallengeConfig deserializeConfig(String configType, String configData) {
        try {
            return switch (configType) {
                case "ROLEPLAY" -> objectMapper.readValue(configData, RoleplayConfig.class);
                case "QUIZ" -> objectMapper.readValue(configData, QuizConfig.class);
                default -> throw new InvalidChallengeConfigException("Tipo de config desconhecido: " + configType);
            };
        } catch (InvalidChallengeConfigException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidChallengeConfigException("Erro ao deserializar config do desafio: " + e.getMessage());
        }
    }
}
