package com.skill.kairo.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skill.kairo.application.port.AIPort;
import com.skill.kairo.application.port.EventPublisherPort;
import com.skill.kairo.application.port.JwtPort;
import com.skill.kairo.application.port.PaymentPort;
import com.skill.kairo.application.service.EvaluateInteractionService;
import com.skill.kairo.application.service.LoginService;
import com.skill.kairo.application.service.PasswordEncoderPort;
import com.skill.kairo.application.service.ProcessSubscriptionService;
import com.skill.kairo.application.service.RegisterService;
import com.skill.kairo.application.usecase.EvaluateInteractionUseCase;
import com.skill.kairo.application.usecase.LoginUseCase;
import com.skill.kairo.application.usecase.ProcessSubscriptionUseCase;
import com.skill.kairo.application.usecase.RegisterUseCase;
import com.skill.kairo.domain.repository.ChallengeRepository;
import com.skill.kairo.domain.repository.GamificationRepository;
import com.skill.kairo.domain.repository.InteractionRepository;
import com.skill.kairo.domain.repository.SubscriptionRepository;
import com.skill.kairo.domain.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeansConfig {

    @Bean
    EvaluateInteractionUseCase evaluateInteractionUseCase(
            ChallengeRepository challengeRepository,
            GamificationRepository gamificationRepository,
            InteractionRepository interactionRepository,
            AIPort aiPort,
            EventPublisherPort eventPublisherPort) {
        return new EvaluateInteractionService(
                challengeRepository, gamificationRepository,
                interactionRepository, aiPort, eventPublisherPort);
    }

    @Bean
    RegisterUseCase registerUseCase(
            UserRepository userRepository,
            GamificationRepository gamificationRepository,
            SubscriptionRepository subscriptionRepository,
            JwtPort jwtPort,
            PasswordEncoderPort passwordEncoderPort) {
        return new RegisterService(
                userRepository, gamificationRepository,
                subscriptionRepository, jwtPort, passwordEncoderPort);
    }

    @Bean
    LoginUseCase loginUseCase(
            UserRepository userRepository,
            JwtPort jwtPort,
            PasswordEncoderPort passwordEncoderPort) {
        return new LoginService(userRepository, jwtPort, passwordEncoderPort);
    }

    @Bean
    ProcessSubscriptionUseCase processSubscriptionUseCase(
            PaymentPort paymentPort,
            SubscriptionRepository subscriptionRepository,
            GamificationRepository gamificationRepository,
            ObjectMapper objectMapper) {
        return new ProcessSubscriptionService(
                paymentPort, subscriptionRepository, gamificationRepository, objectMapper);
    }
}
