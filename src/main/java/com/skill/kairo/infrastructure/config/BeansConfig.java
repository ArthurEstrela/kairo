package com.skill.kairo.infrastructure.config;

import com.skill.kairo.application.port.AIPort;
import com.skill.kairo.application.port.EventPublisherPort;
import com.skill.kairo.application.service.EvaluateInteractionService;
import com.skill.kairo.domain.repository.ChallengeRepository;
import com.skill.kairo.domain.repository.GamificationRepository;
import com.skill.kairo.domain.repository.InteractionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeansConfig {

    @Bean
    public EvaluateInteractionService evaluateInteractionService(
            ChallengeRepository challengeRepository,
            GamificationRepository gamificationRepository,
            InteractionRepository interactionRepository,
            AIPort aiPort,
            EventPublisherPort eventPublisherPort
    ) {
        // O Spring injeta as instâncias reais (JpaAdapters, OpenAIAdapter, etc.) aqui.
        // O nosso Service purista ganha vida sem nunca se sujar com anotações de framework!
        return new EvaluateInteractionService(
                challengeRepository,
                gamificationRepository,
                interactionRepository,
                aiPort,
                eventPublisherPort
        );
    }
}